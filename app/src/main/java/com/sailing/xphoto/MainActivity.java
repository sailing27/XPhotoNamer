package com.sailing.xphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.sailing.xphoto.engine.XFileRenamerAsynTask;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainActivity extends ActionBarActivity implements DirectoryChooserFragment.OnFragmentInteractionListener{
    private static Logger logger = LoggerFactory.getLogger(MainActivity.class);

    /**文件选择对话框*/
    private DirectoryChooserFragment mDialog;

    private XPreferencesHelper preferencesHelper = null;

    /**Adapter*/
    private FolderListAdapter mDataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView folderListView = (ListView) findViewById(R.id.folder_list_view);

        preferencesHelper = new XPreferencesHelper(this);
        mDataAdapter = new FolderListAdapter(this, preferencesHelper);
        folderListView.setAdapter(mDataAdapter);

        //添加删除处理
        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDataAdapter.remove(position);
            }
        });

        final MainActivity mainActivity = this;
        Button button = (Button) findViewById(R.id.start_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.info("Start to process...");
                XFileRenamerAsynTask ayncTask = new XFileRenamerAsynTask(mainActivity, preferencesHelper );
                ayncTask.execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //设置
        if (id == R.id.action_settings) {
            logger.info("Setting...");

            Intent intent = new Intent(this, XFragmentPreferences.class);
            startActivity(intent);

            return true;
        }
        else if (id==R.id.action_add){  //添加文件夹
            logger.info("Add...");
            mDialog = DirectoryChooserFragment.newInstance("Choose Folder", null);
            mDialog.show(getFragmentManager(), null);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 更新进度
     * @param position
     * @param progress
     */
    public void updateProgress(int position, int progress) {
        if (null == mDataAdapter) {
            logger.error("mDataAdapter is null");
            return;
        }
        mDataAdapter.updateProgress(position, progress);
    }


    /**
     * 选择一个目录。
     * 将目录存放SharePreferences。
     * @param path
     */
    @Override
    public void onSelectDirectory(String path) {
        logger.info("Select path:" + path);
        //关闭对话框
        mDialog.dismiss();

        if (null == path) {
            logger.error("path is null.");
            return;
        }
        //保存目录到SharePreferences
        preferencesHelper.saveFolderInfo(path);

        //更新显示数据
        if (null !=mDataAdapter) {
            logger.info("Update data:" + path);
            mDataAdapter.addPath(path);
        }
    }

    @Override
    public void onCancelChooser() {
        //关闭对话框
        mDialog.dismiss();
    }

}
