package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthydemo.ArticleDetailActivity;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.ArticleData;
import com.squareup.picasso.Picasso;
import java.util.List;


public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>{

    private static final String TAG = "ArticleAdapter";
    private Context context;
    private List<ArticleData.ArticleListBean> articleDataList;

    public ArticleAdapter(Context context, List<ArticleData.ArticleListBean> articleDataList) {
        this.context = context;
        this.articleDataList = articleDataList;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_item, parent, false);
        return new ArticleAdapter.ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.textView.setText(articleDataList.get(position).getTitle());
        Picasso.get().load(articleDataList.get(position).getImg()).resize(600, 900).onlyScaleDown().into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, ArticleDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("HTML", articleDataList.get(position).getHtml());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articleDataList.size();
    }


    public class ArticleViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView  textView;
        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageArtIcon);
            textView = itemView.findViewById(R.id.tv_art_title);
        }
    }
}
