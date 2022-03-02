package api.adhyay.app.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;
import api.adhyay.app.libs.PeerConnectionUtils;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.databinding.PeerVideoItemsBinding;
import api.adhyay.app.videocall.vm.PeerProps;


public class PeerView extends RelativeLayout {
    private static final String TAG = PeerView.class.getName();

    public PeerView(Context context) {
        super(context);
        init(context);
    }

    public PeerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PeerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PeerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    PeerVideoItemsBinding videoItemsBinding;

    private void init(Context context) {
        videoItemsBinding =
                DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.peer_video_items, this, true);
        videoItemsBinding.peerView.videoRenderer.init(PeerConnectionUtils.getEglContext(), null);
    }

    public void setProps(PeerProps props) {
        // set view model into included layout

        videoItemsBinding.peerView.setPeerViewProps(props);
        videoItemsBinding.setPeerProps(props);
    }
}
