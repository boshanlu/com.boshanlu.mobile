package com.boshanlu.mobile.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.fragment.FrageHistory;
import com.boshanlu.mobile.fragment.FrageTopicStar;
import com.boshanlu.mobile.model.FrageType;

public class FragementActivity extends BaseActivity {

    public static void open(Context c, int type) {
        Intent intent = new Intent(c, FragementActivity.class);
        intent.putExtra("TYPE", type);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout f = new FrameLayout(this);
        f.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        f.setId(R.id.container);
        setContentView(f);

        Fragment to = null;
        Bundle b = getIntent().getExtras();
        switch (b.getInt("TYPE")) {
            case FrageType.TOPIC:
                to = FrageTopicStar.newInstance(FrageType.TOPIC);
                break;
            case FrageType.START:
                to = FrageTopicStar.newInstance(FrageType.START);
                break;
            case FrageType.HISTORY:
                to = new FrageHistory();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, to).commit();
    }

}
