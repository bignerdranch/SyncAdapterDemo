package testapp.bgardner.syncadapterdemo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import testapp.bgardner.syncadapterdemo.R;
import testapp.bgardner.syncadapterdemo.contentprovider.ContentProviderContract;
import testapp.bgardner.syncadapterdemo.interfaces.AuthenticatedInterface;
import testapp.bgardner.syncadapterdemo.models.BookErratum;

public class BookErrataFragment extends Fragment {
    private static final String TAG = "BookErrataFragment";
    private static final String BOOK_ERRATA_ARG = "BookErrataFragment.BookErrata";

    private BookErratum mBookErratum;
    private EditText mCourseNameView;
    private EditText mPageNumberView;
    private EditText mBookVersionView;
    private EditText mErrorDescriptionView;
    private TextView mUserView;
    private Button mDeleteButton;
    private AuthenticatedInterface mActivity;

    public static BookErrataFragment newInstance(BookErratum errata) {
        Bundle args = new Bundle();
        args.putSerializable(BOOK_ERRATA_ARG, errata);
        BookErrataFragment fragment = new BookErrataFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AuthenticatedInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookErratum = (BookErratum) getArguments().get(BOOK_ERRATA_ARG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_errata, container, false);

        mCourseNameView = (EditText) view.findViewById(R.id.erratum_course_name_view);
        mCourseNameView.setText(mBookErratum.getCourseName());
        mCourseNameView.addTextChangedListener(mCourseNameTextWatcher);

        mPageNumberView = (EditText) view.findViewById(R.id.erratum_page_number_view);
        mPageNumberView.setText(Integer.toString(mBookErratum.getPageNumber()));
        mPageNumberView.addTextChangedListener(mPageNumberTextWatcher);

        mBookVersionView = (EditText) view.findViewById(R.id.erratum_book_version_view);
        mBookVersionView.setText(mBookErratum.getBookVersion());
        mBookVersionView.addTextChangedListener(mBookVersionTextWatcher);

        mErrorDescriptionView = (EditText) view.findViewById(R.id.erratum_error_description_view);
        mErrorDescriptionView.setText(mBookErratum.getErrorDescription());
        mErrorDescriptionView.addTextChangedListener(mErrorDescriptionTextWatcher);

        mUserView = (TextView) view.findViewById(R.id.erratum_user_text_view);
        mUserView.setText(getCurrentUsersUsername());

        mDeleteButton = (Button) view.findViewById(R.id.book_erratum_delete_button);

        if (!userCanEditErratum()) {
            disableEditTexts();
            disableDeleteButton();
        } else {
            hookUpDeleteButton();
        }

        return view;
    }

    private boolean userCanEditErratum() {
        int userId = mBookErratum.getUserId();
        Uri userUri = Uri.withAppendedPath(ContentProviderContract.User.CONTENT_URI, "" + userId);
        Cursor cursor = getActivity().getContentResolver().query(userUri, null, null, null, null);
        cursor.moveToFirst();
        String username = cursor.getString(cursor.getColumnIndex(ContentProviderContract.User.USERNAME));
        return username.equals(getCurrentUsersUsername());
    }

    private void disableEditTexts() {
        mCourseNameView.setEnabled(false);
        mPageNumberView.setEnabled(false);
        mBookVersionView.setEnabled(false);
        mErrorDescriptionView.setEnabled(false);
    }

    private void disableDeleteButton() {
        mDeleteButton.setEnabled(false);
    }

    private void hookUpDeleteButton() {
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selection = ContentProviderContract.BookErrata.ERRATA_ID + " = ?";
                String[] selectionParams = {Integer.toString(mBookErratum.getId())};
                int updated = getActivity().getContentResolver().update(ContentProviderContract.BookErrata.CONTENT_URI, BookErratum.getDeleteContentValues(), selection, selectionParams);
            }
        });
    }

    private String getCurrentUsersUsername() {
        return mActivity.getCurrentUsersUsername();
    }

    private TextWatcher mCourseNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mBookErratum.setCourseName(s.toString());
            updateBookErratumDatabaseRecord();
        }
    };

    private TextWatcher mPageNumberTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
                mBookErratum.setPageNumber(Integer.valueOf(s.toString()));
                updateBookErratumDatabaseRecord();
            }
        }
    };

    private TextWatcher mBookVersionTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mBookErratum.setBookVersion(s.toString());
            updateBookErratumDatabaseRecord();
        }
    };

    private TextWatcher mErrorDescriptionTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mBookErratum.setErrorDescription(s.toString());
            updateBookErratumDatabaseRecord();
        }
    };

    private class BookErratumUpdaterTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ContentValues cv = mBookErratum.getUpdateContentValues();
            Uri erratumUri = Uri.withAppendedPath(ContentProviderContract.BookErrata.CONTENT_URI, Integer.toString(mBookErratum.getId()));
            int updatedRows = getActivity().getContentResolver().update(erratumUri, cv, null, null);
            Log.d(TAG, "Updated row with content values: " + cv);
            Log.d(TAG, "Updated this many errata rows: " + updatedRows);
            return null;
        }
    }

    private void updateBookErratumDatabaseRecord() {
        new BookErratumUpdaterTask().execute();
    }
}
