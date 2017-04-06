package com.huwenmin.playerexample;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huwenmin.playerexample.listener.SampleListener;
import com.huwenmin.playerexample.widget.ScrollWebView;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.widget.NestedScrollView.*;

public class ExampleActivity extends AppCompatActivity {


    @BindView(R.id.scroll_webView)
    ScrollWebView webView;
    @BindView(R.id.web_top_layout)
    NestedScrollView webTopLayout;
    @BindView(R.id.web_player)
    NormalGSYVideoPlayer webPlayer;
    @BindView(R.id.web_top_layout_video)
    RelativeLayout webTopLayoutVideo;

    private String exampleUrl = "http://baobab.wdjcdn.com/14564977406580.mp4";

    private boolean isPlay;
    private boolean isPause;
    private boolean isSamll;

    private OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        ButterKnife.bind(this);

        String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
        //String url = "https://d131x7vzzf85jg.cloudfront.net/upload/documents/paper/b2/61/00/00/20160420_115018_b544.mp4";
        webPlayer.setUp(url, false, null, "测试视频");

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        webPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, webPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        webPlayer.setIsTouchWiget(true);
        //关闭自动旋转
        webPlayer.setRotateViewAuto(false);
        webPlayer.setLockLand(false);
        webPlayer.setShowFullAnimation(false);
        webPlayer.setNeedLockFull(true);
        //detailPlayer.setOpenPreView(true);
        webPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                webPlayer.startWindowFullscreen(ExampleActivity.this, true, true);
            }
        });

        webPlayer.setStandardVideoAllCallBack(new SampleListener() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                //开始播放了才能旋转和全屏
                orientationUtils.setEnable(true);
                isPlay = true;
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
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

        webPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl("https://www.baidu.com");


        webTopLayout.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!webPlayer.isIfCurrentIsFullscreen() && scrollY >= 0 && isPlay) {
                    if (scrollY > webPlayer.getHeight()) {
                        //如果是小窗口就不需要处理
                        if (!isSamll) {
                            isSamll = true;
                            int size = CommonUtil.dip2px(ExampleActivity.this, 150);
                            webPlayer.showSmallVideo(new Point(size, size), true, true);
                            orientationUtils.setEnable(false);
                        }
                    } else {
                        if (isSamll) {
                            isSamll = false;
                            orientationUtils.setEnable(true);
                            //必须
                            webTopLayoutVideo.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    webPlayer.hideSmallVideo();
                                }
                            }, 50);
                        }
                    }
                    webTopLayoutVideo.setTranslationY((scrollY <= webTopLayoutVideo.getHeight()) ? -scrollY : -webTopLayoutVideo.getHeight());
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }

        if (webPlayer.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webPlayer.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause && !isSamll) {
            if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
                if (!webPlayer.isIfCurrentIsFullscreen()) {
                    webPlayer.startWindowFullscreen(ExampleActivity.this, true, true);
                }
            } else {
                //新版本isIfCurrentIsFullscreen的标志位内部提前设置了，所以不会和手动点击冲突
                if (webPlayer.isIfCurrentIsFullscreen()) {
                    webPlayer.backFromWindowFull(this);
                }
                if (orientationUtils != null) {
                    orientationUtils.setEnable(true);
                }
            }
        }
    }


    private void resolveNormalVideoUI() {
        //增加title
        webPlayer.getTitleTextView().setVisibility(View.GONE);
        webPlayer.getTitleTextView().setText("测试视频");
        webPlayer.getBackButton().setVisibility(View.GONE);
    }
}
