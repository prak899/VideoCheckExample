package api.adhyay.app.videocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;
import api.adhyay.app.libs.PeerConnectionUtils;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.databinding.ViewMeBinding;
import api.adhyay.app.videocall.vm.MeProps;


public class MeView extends RelativeLayout {
    public MeView(Context context) {
        super(context);
        init(context);
    }

    public MeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    ViewMeBinding mBinding;

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_me, this, true);
        mBinding.meView.videoRenderer.init(PeerConnectionUtils.getEglContext(), null);
    }

    public void setProps(MeProps props) {

        mBinding.meView.setPeerViewProps(props);

        mBinding.meView.videoRenderer.setZOrderMediaOverlay(true);

        mBinding.setMeProps(props);
    }
}
