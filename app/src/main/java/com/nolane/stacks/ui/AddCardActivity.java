package com.nolane.stacks.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nolane.stacks.R;
import com.nolane.stacks.utils.UriUtils;

import static com.nolane.stacks.provider.CardsContract.*;

/**
 * This activity is for adding new cards into existing stack. <br>
 * Required: <br>
 * data type: {@link Stacks#CONTENT_ITEM_TYPE}
 */
public class AddCardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        if (null == savedInstanceState) {
            UriUtils.checkDataTypeOrThrow(this, Stacks.CONTENT_ITEM_TYPE);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_root, new AddCardFragment())
                    .commit();
        }
    }
}
