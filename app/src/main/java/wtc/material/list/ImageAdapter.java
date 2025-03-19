package wtc.material.list;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.modern.chating.R;

import java.util.List;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.io.File;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<WtcModal> imagePaths; // List path gambar dari storage
    private Context context;
    OnClick onClick;

    public ImageAdapter(Context context, List<WtcModal> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(view);
    }

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        WtcModal data = imagePaths.get(position);
        setImageLoad(data.getFile(), holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onClick(new WtcModal(data.getFile()), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_recycler);
        }
    }

    public interface OnClick {
        void onClick(WtcModal wtcModal, int position);
    }

    private void setImageLoad(String filePath, ImageView image){
        File file = new File(filePath);
        if(file.exists()){
            if(filePath.isEmpty()){
                Glide.with(context)
                        .load(R.drawable.ic_cloud_data)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                        .into(image);
            }else{
                Glide.with(context)
                        .load(filePath)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(10)))
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                                image.setImageDrawable(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
        }else{
            Glide.with(context)
                    .load(R.drawable.ic_cloud_data)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .into(image);
        }

    }
}
