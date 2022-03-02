package api.adhyay.app.videocall.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import androidx.databinding.BindingAdapter;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.vm.MeProps;


public class BindingAdapters {

    private static final String TAG = BindingAdapters.class.getName();

    @BindingAdapter({"edias_mic_state"})
    public static void deviceMicState(ImageView imageView, MeProps.DeviceState state) {
        if (state == null) {
            return;
        }

        switch (state) {
            case ON:
                imageView.setImageResource(R.drawable.ic_mic_on);
                break;
            case OFF:
            case UNSUPPORTED:
                imageView.setImageResource(R.drawable.ic_mic_off);
                break;
        }
    }

    @BindingAdapter({"edias_change_came_state"})
    public static void changeCamState(View view, MeProps.DeviceState state) {
        if (state == null) {
            return;
        }
        Log.d(TAG, "edias_change_came_state: " + state.name());
        view.setEnabled(MeProps.DeviceState.ON.equals(state));
    }

    @BindingAdapter({"edias_cam_state"})
    public static void deviceCamState(ImageView imageView, MeProps.DeviceState state) {
        if (state == null) {
            return;
        }
        Log.d(TAG, "edias_cam_state: " + state.name());

        switch (state) {
            case ON:
                imageView.setImageResource(R.drawable.ic_video_on);
                break;
            case OFF:
            case UNSUPPORTED:
               imageView.setImageResource(R.drawable.ic_video_off);
                break;
        }
    }

    @BindingAdapter({"bind:edias_audio_muted"})
    public static void audioMuted(ImageView view, boolean audioMuted) {

        Log.d(TAG, "audioMuted: "+audioMuted);

        if (!audioMuted) {
   //         view.setBackgroundResource(R.drawable.bg_left_box_off);
            view.setImageResource(R.drawable.ic_audio_on);
        } else {
     //       view.setBackgroundResource(R.drawable.bg_left_box_on);
            view.setImageResource(R.drawable.ic_audio_off);
        }
    }

    @BindingAdapter({"edias_render"})
    public static void render(SurfaceViewRenderer renderer, VideoTrack track) {

        Log.d(TAG, "render: "+track);

        if (track != null) {
            track.addSink(renderer);
            renderer.setVisibility(View.VISIBLE);
        } else {
            renderer.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"edias_render_empty"})
    public static void renderEmpty(View renderer, VideoTrack track) {
        Log.d(TAG, "edias_render_empty: " + (track != null));
        if (track == null) {
            renderer.setVisibility(View.VISIBLE);
        } else {
            renderer.setVisibility(View.GONE);
        }
    }
}
