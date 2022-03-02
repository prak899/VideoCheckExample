package api.adhyay.app.videocall.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import api.adhyay.app.libs.RoomStore;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.model.Peer;
import api.adhyay.app.videocall.view.PeerView;
import api.adhyay.app.videocall.vm.PeerProps;


public class PeerVideoAdapter extends RecyclerView.Adapter<PeerVideoAdapter.VideoViewHolder> {


    private static final String TAG = PeerVideoAdapter.class.getName();
    @NonNull
    private RoomStore mStore;
    @NonNull private LifecycleOwner mLifecycleOwner;
    public List<Peer> mPeers = new LinkedList<>();

    private int containerHeight;

    public PeerVideoAdapter(
            @NonNull RoomStore store,
            @NonNull LifecycleOwner lifecycleOwner) {
        mStore = store;
        mLifecycleOwner = lifecycleOwner;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void replacePeers(@NonNull List<Peer> peers) {
        mPeers.clear();
        mPeers = peers;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        containerHeight = parent.getHeight();
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_remote_view, parent, false);
        return new VideoViewHolder(
                view, new PeerProps(((AppCompatActivity) context).getApplication(), mStore));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.mPeerView.getLayoutParams();
        layoutParams.height = getItemHeight();
        holder.mPeerView.setLayoutParams(layoutParams);
        // bind
        holder.bind(mLifecycleOwner, mPeers.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return mPeers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private int getItemHeight() {
        int itemCount = getItemCount();
        if (itemCount <= 1) {
            return containerHeight;
        } else if (itemCount <= 3) {
            return containerHeight/itemCount;
        } else {
            return (containerHeight/2);
        }
    }

     class VideoViewHolder extends RecyclerView.ViewHolder {

        private final String TAG = VideoViewHolder.class.getName();
        private PeerView mPeerView;
        @NonNull PeerProps mPeerProps;

        public VideoViewHolder(@NonNull View itemView, @NonNull PeerProps props) {
            super(itemView);
            mPeerProps = props;
            mPeerView= itemView.findViewById(R.id.remote_peer);
        }

        void bind(LifecycleOwner owner, @NonNull String peer) {
            Log.d(TAG, "bind() id: " + peer + ", video track: "+mPeerProps.getVideoTrack());
            mPeerProps.connect(owner, peer);
            mPeerView.setProps(mPeerProps);
        }
    }
}
