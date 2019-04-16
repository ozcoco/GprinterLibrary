package com.yf.btp.widgets;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @Name DecorativeAdapter
 * @package
 * @Author oz
 * @Email 857527916@qq.com
 * @Time 2018/7/21 17:46
 * @Description todo
 */
public class DecorativeAdapter<VH extends RecyclerView.ViewHolder, E> extends RecyclerView.Adapter<VH> {

    private final IAdapterDecorator adapterDecorator;
    private final LayoutInflater inflater;
    private final Context context;
    private List<E> data = new ArrayList<>();

    public DecorativeAdapter(@NonNull Context context, @NonNull IAdapterDecorator adapterDecorator) {
        this.inflater = LayoutInflater.from(context);

        this.context = context;

        this.adapterDecorator = adapterDecorator;

    }

    public void setData(Collection<E> data) {

        if (data == null) return;

        if (this.data == null) this.data = new ArrayList<>();

        this.data.clear();

        this.data.addAll(data);

        notifyDataSetChanged();

    }


    public void clearData() {

        if (data == null) return;

        this.data.clear();

        notifyDataSetChanged();
    }

    public void addData(List<E> data) {

        if (data == null) return;

        if (this.data == null) this.data = new ArrayList<>();

        this.data.addAll(data);

        notifyDataSetChanged();

    }

    public void addData(E data) {

        if (data == null) return;

        if (this.data == null) this.data = new ArrayList<>();

        this.data.add(data);

        notifyDataSetChanged();

    }

    public void addData(int index, List<E> data) {

        if (data == null) return;

        assert index > 0;

        if (this.data == null) this.data = new ArrayList<>();

        this.data.addAll(index, data);

        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return (VH) adapterDecorator.onCreateViewHolder(context, inflater, parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        adapterDecorator.onBindViewHolder(context, holder, getItem(position), position);

    }

    @Override
    public int getItemCount() {
        if (data == null) data = new ArrayList<>();

        return data.size();
    }

    private E getItem(int position) {
        return data.get(position);

    }

    public interface IAdapterDecorator<VH extends RecyclerView.ViewHolder, E> {

        VH onCreateViewHolder(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType);

        void onBindViewHolder(@NonNull Context context, @NonNull VH holder, @NonNull E data, int position);

    }

}

