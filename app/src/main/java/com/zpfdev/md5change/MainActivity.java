package com.zpfdev.md5change;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StatFs;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar mTopAppBar;
    private LinearLayout mLinearLayout3;
    private TextView mContent;
    private TextView fileList;
    private LinearLayout mGetFileList;
    private LinearLayout mChangeFileMd5;
    private RecyclerView recyclerView;
    private File file;
    private File file2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTopAppBar = findViewById(R.id.topAppBar);
        mLinearLayout3 = findViewById(R.id.linearLayout3);
        mContent = findViewById(R.id.content);
        mGetFileList = findViewById(R.id.getFileList);
        mChangeFileMd5 = findViewById(R.id.changeFileMd5);
        recyclerView = findViewById(R.id.recyclerView);
        fileList = findViewById(R.id.fileList);
        file = new File(getExternalFilesDir(null), "源文件");
        file2 = new File(getExternalFilesDir(null), "修改后");
        mTopAppBar.getMenu().findItem(R.id.action_github).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zhao-pf/Md5Change")));
                return false;
            }
        });
        mTopAppBar.getMenu().findItem(R.id.action_call).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ClipboardManager systemService = (ClipboardManager) getSystemService(AppCompatActivity.CLIPBOARD_SERVICE);
                ClipData label = ClipData.newPlainText("Label", "zpf6307");
                systemService.setPrimaryClip(label);
                PopTip.show("已复制微信");
                return false;
            }
        });

        if (!file.exists()) {
            file.mkdir();
        }
        if (!file2.exists()) {
            file2.mkdir();
        }
        new FileObserver(file.getPath()) {
            @Override
            public void onEvent(int i, @Nullable String s) {

            }
        }.startWatching();
        mContent.setText("1.手动把修改的文件放入下方文件夹\n" + file + "\n2.软件自动获取文件(失败请手动获取)\n3.点击批量更改md5\n4.在下方的文件夹中获取输出文件\n" + file2);
        mGetFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("数据", "onEvent: asda ");
                getFileList();
            }
        });
        mChangeFileMd5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaitDialog.show("修改中...");
                File[] files = file.listFiles();
                if ((files.length != 0)) {
                    for (int i = 0; i < files.length; i++) {
                        getNewFIleMd5(files[i], i, files.length);
                    }
                } else {
                    PopTip.show("没有可以修改的文件");
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFileList();
    }

    private void getFileList() {
        File[] files = file.listFiles();
        File[] files2 = file2.listFiles();
        fileList.setText("文件列表(" + files.length + ")");
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(getLayoutInflater().inflate(R.layout.item_list, parent, false)) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                File file1 = files[position];
                LinearLayout mMainClick;
                TextView mTextView;
                TextView mTextView2;
                TextView textView3;
                TextView fileSize;
                mMainClick = holder.itemView.findViewById(R.id.mainClick);
                mTextView = holder.itemView.findViewById(R.id.textView);
                mTextView2 = holder.itemView.findViewById(R.id.textView2);
                textView3 = holder.itemView.findViewById(R.id.textView3);
                fileSize = holder.itemView.findViewById(R.id.fileSize);
                mTextView.setText(file1.getName());


                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
                decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                String size = decimalFormat.format(file1.length() / 1024f / 1024f) + "MB";
                fileSize.setText(size);

                new Thread(() -> {
                    String s1 = CalcMD5.calcMD5(file1);
                    runOnUiThread(() -> mTextView2.setText("md5:" + s1));
                    if (files2.length != 0) {
                        File file = files2[position];
                        String s2 = CalcMD5.calcMD5(file);
                        runOnUiThread(() -> textView3.setText("md5:" + s2 + "(修改后)"));
                    }
                }).start();
            }

            @Override
            public int getItemCount() {
                return files.length;
            }
        });
    }


    public long getLocalSize() {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return 0;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (((stat.getAvailableBlocksLong() * stat.getBlockSizeLong()) / 1024) / 1024) / 1024;
    }

    /* access modifiers changed from: private */
    public void getNewFIleMd5(File oldFile, int i, int length) {
        try {
            if (!oldFile.exists() || !oldFile.isFile()) {
                Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show();
            } else if (((oldFile.length() / 1024) / 1024) / 1024 > getLocalSize()) {
                Toast.makeText(this, "本地空间不足", Toast.LENGTH_SHORT).show();
            } else {
                final File nowFile = new File(file2 + "/" + oldFile.getName());
                if (nowFile.exists()) {
                    nowFile.delete();
                }
                nowFile.createNewFile();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            FileInputStream in = new FileInputStream(oldFile.getPath());
                            FileOutputStream out = new FileOutputStream(nowFile.getPath());
                            byte[] b = new byte[1024];
                            while (true) {
                                int n = in.read(b);
                                if (n != -1) {
                                    out.write(b, 0, n);
                                } else {
                                    out.write(new byte[]{0});
                                    in.close();
                                    out.close();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (length - 1 == i) {
                                                TipDialog.show("修改成功", WaitDialog.TYPE.SUCCESS);
                                                getFileList();
                                            }
                                        }
                                    });
                                    return;
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
        }
    }

}