package com.huwenmin.playerexample;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.huwenmin.playerexample.listener.SampleListener;
import com.huwenmin.playerexample.video.WasuVideoPlayer;
import com.wasu.videoplayer.utils.Debuger;
import com.wasu.videoplayer.video.VideoManager;
import com.wasu.videoplayer.listener.LockClickListener;
import com.wasu.videoplayer.utils.OrientationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ExampleActivity extends AppCompatActivity {


    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    //推荐使用StandardGSYVideoPlayer，功能一致
    @BindView(R.id.detail_player)
    WasuVideoPlayer detailPlayer;

    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;


    private String url = "http://baobab.wdjcdn.com/14564977406580.mp4";


    private boolean isLandscape =true ; //默认进来为横屏

    private OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLandscape) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        Debuger.printfError("onCreate");
        setContentView(R.layout.activity_example);
        ButterKnife.bind(this);


        Debuger.enable();
        detailPlayer.setLandscape(isLandscape);
        url = "http://apkvod-cnc.wasu.cn/591002be/6a2b9345001032a31d733d4b17113db6/pcsan02/ipad/268/20111201/201112011155213125_785322_b7d6d662/playlist.m3u8?k=26414a16030fadfacdcfa7ec4a5c577b&su=FsOSoD1WiIQ9SogO/xnAQg==&uid=ff29b433d5c250017837a499c13ac00b&tn=82352682&t=2449e411800a83ac789a31ccc8919b70&src=wasu.cn&cid=1&vid=null&WS00001=10000&em=3";

        detailPlayer.setUp(url, false, null, "测试视频");
        if (isLandscape) detailPlayer.startWindowFullscreen(this,true,true);
        VideoManager.instance(ExampleActivity.this).setTimeOut(20000, true);
//
        resolveNormalVideoUI();
//
//        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
//        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        detailPlayer.setIsTouchWiget(true);

//        //关闭自动旋转
        detailPlayer.setRotateViewAuto(false);
//        //一全屏就自动横屏
        detailPlayer.setLockLand(false);
//        //设置全屏动画
        detailPlayer.setShowFullAnimation(false);
//        //是否需要全屏锁定屏幕功能
        detailPlayer.setNeedLockFull(true);
        //初始化状态
//        detailPlayer.initUIState();


        //没有剧集信息
//        detailPlayer.isShowEpisode(false);


        //设置一开始就是横屏进入的
//        orientationUtils.setLandscape(isLandscape);
        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                detailPlayer.startWindowFullscreen(ExampleActivity.this, true, true);
            }
        });

        detailPlayer.setStandardVideoAllCallBack(new SampleListener() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);

                if (!isLandscape) orientationUtils.setEnable(true);
                else {
                    detailPlayer.setRotateViewAuto(false);
                    orientationUtils.setEnable(false);
                }
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                detailPlayer.release();
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
                super.onClickStartError(url, objects);
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }

        });

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null ) {
                    //配合下方的onConfigurationChanged
                   if (!isLandscape) orientationUtils.setEnable(!lock);
                    else orientationUtils.setEnable(false);
                }
            }
        });

        //获取当前的时长
        detailPlayer.getCurrentPositionWhenPlaying();
        //从自己记录的时间开始播放
//        detailPlayer.setSeekOnStart();
        //这个是自动播放
        detailPlayer.startPlayLogic();
    }

    @Override
    public void onBackPressed() {
        if (!isLandscape) {
            if (orientationUtils != null) {
                orientationUtils.backToProtVideo();
            }
            if (detailPlayer.backFromWindowFull(this)) {
                return;
            }
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Debuger.printfError("onPause");
        detailPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debuger.printfError("onResume");

        detailPlayer.onVideoResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debuger.printfError("onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debuger.printfError("onDestroy");
        detailPlayer.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            if (!detailPlayer.isIfCurrentIsFullscreen()) {
                detailPlayer.startWindowFullscreen(ExampleActivity.this, true, true);
            }
        } else {
           if (!isLandscape) {//新版本isIfCurrentIsFullscreen的标志位内部提前设置了，所以不会和手动点击冲突
               if (detailPlayer.isIfCurrentIsFullscreen()) {
                   detailPlayer.backFromWindowFull(this);
               }
               if (orientationUtils != null) {
                   orientationUtils.setEnable(true);
               }
           }
        }
    }


    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.INVISIBLE);
        detailPlayer.getTitleTextView().setText("测试视频");


    }
}
