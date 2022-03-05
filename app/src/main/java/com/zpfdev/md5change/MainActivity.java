package com.zpfdev.md5change;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar mTopAppBar;
    private LinearLayout mLinearLayout3;
    private TextView mContent;
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
        file = new File(getExternalFilesDir(null), "源文件");
        file2 = new File(getExternalFilesDir(null), "修改后");
        if (!file.exists()) {
            file.mkdir();
        }
        if (!file2.exists()) {
            file2.mkdir();
        }

        mContent.setText("1.手动把修改的文件放入下面文件夹\n" + file + "\n2.点击获取文件列表\n3.点击批量更改md5\n4.在下面的文件夹中获取输出文件\n" + file2);
        mGetFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File[] files = file.listFiles();
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
                        mMainClick = holder.itemView.findViewById(R.id.mainClick);
                        mTextView = holder.itemView.findViewById(R.id.textView);
                        mTextView2 = holder.itemView.findViewById(R.id.textView2);
                        mTextView.setText(file1.getName());
                        mTextView2.setText("md5:" + CalcMD5.calcMD5(file1));
                    }

                    @Override
                    public int getItemCount() {
                        return files.length;
                    }
                });


            }
        });
        mChangeFileMd5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaitDialog.show("修改中...");
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    getNewFIleMd5(files[i], i, files.length);
                }

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