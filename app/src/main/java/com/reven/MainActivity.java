package com.reven;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.reven.widget.DrawView;
import com.utils.BitmapUtils;
import com.utils.FileUtils;
import com.xm.activity.base.XMBaseActivity;
import com.xm.activity.base.XMBasePresenter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends XMBaseActivity {
    @BindView(R.id.draw_view)
    DrawView drawView;

    @Override
    public XMBasePresenter getPresenter() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityPermissionsDispatcher.initViewWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void initView() {
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_save)
    void onSave(View view) {
        Bitmap bitmap = viewToBitmap(drawView);
        if (bitmap != null) {
            boolean bRet = saveBitmapToSd(bitmap, Environment.getExternalStorageDirectory() + File.separator + "drawView.png");
            if (bRet) {
                showToast("图片保存成功", Toast.LENGTH_LONG);
            }else {
                showToast("图片保存失败",Toast.LENGTH_LONG);
            }
        }
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);
    }

    /**
     * View 转换成 Bitmap
     * @param view
     * @return
     */
    private Bitmap viewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 将bitmap保存到sd卡
     * @param bitmap
     */
    private boolean saveBitmapToSd(Bitmap bitmap,String savePath) {
        File file = new File(savePath);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}