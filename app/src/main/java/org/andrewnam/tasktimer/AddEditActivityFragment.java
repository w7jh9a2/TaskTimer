package org.andrewnam.tasktimer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";

    public enum FragmentEditMode {EDIT, ADD }
    private FragmentEditMode mMode;

    private EditText mNameTextView;
    private EditText mDescriptionTextView;
    private EditText mSortOrderTextView;
    private Button mSaveButton;

    public AddEditActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        mNameTextView = (EditText) view.findViewById(R.id.addedit_name);
        mDescriptionTextView = (EditText) view.findViewById(R.id.addedit_description);
        mSortOrderTextView = (EditText) view.findViewById(R.id.addedit_sortorder);
        mSaveButton = (Button) view.findViewById(R.id.addedit_save);
        
        Bundle arguments = getActivity().getIntent().getExtras(); // The line we'll change later
        
        final Task task;
        if(arguments != null) {
            Log.d(TAG, "onCreateView: retrieving task details.");

            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if(task != null) {
                Log.d(TAG, "onCreateView: Task details found, editing...");
                mNameTextView.setText(task.getName());
                mDescriptionTextView.setText(task.getDescription());
                mSortOrderTextView.setText(Integer.toString(task.getSortOrder()));
                mMode = FragmentEditMode.EDIT;
            } else {
                // No task, so we must be adding a new task and not editing an existing one
                mMode = FragmentEditMode.ADD;
            }
        } else {
            task = null;
            Log.d(TAG, "onCreateView: No arguments, adding new record");
            mMode = FragmentEditMode.ADD;
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update the database if at least one field has changed.
                // - There's no need to hit the database unless this has happened.
                int so; // to save repeated conversions to int.
                if(mSortOrderTextView.length() > 0) {
                    so = Integer.parseInt(mSortOrderTextView.getText().toString());
                } else {
                    so = 0;
                }

                ContentResolver contentResolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues();

                switch(mMode) {
                    case EDIT:
                        if(!mNameTextView.getText().toString().equals(task.getName())) {
                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                        }
                        if(!mDescriptionTextView.getText().toString().equals(task.getDescription())) {
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                        }
                        if(so != task.getSortOrder()) {
                            values.put(TasksContract.Columns.TASKS_SORTORDER, so);
                        }
                        if(values.size() != 0) {
                            Log.d(TAG, "onClick: updating task");
                            contentResolver.update(TasksContract.buildTaskUri(task.getId()), values, null, null);
                        }
                        break;
                    case ADD:
                        if(mNameTextView.length() > 0) {
                            Log.d(TAG, "onClick: adding new task");
                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_SORTORDER, so);
                            contentResolver.insert(TasksContract.CONTENT_URI, values);
                        }
                        break;
                }
                Log.d(TAG, "onClick: Done editing");
            }
        });
        Log.d(TAG, "onCreateView: Exiting...");

        return view;
    }
}
