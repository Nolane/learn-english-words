package com.nolane.stacks.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nolane.stacks.R;
import com.nolane.stacks.utils.RecyclerCursorAdapter;
import com.nolane.stacks.utils.UriUtils;

import static com.nolane.stacks.provider.CardsContract.Stacks;

/**
 * This fragment finds out which stack user wants to train and then
 * start {@link TrainingActivity} with this stack as data.
 */
public class PickStackFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Adapter for the RecyclerView.
     */
    public class StacksAdapter extends RecyclerCursorAdapter<StacksAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public View root;
            public ImageView ivIcon;
            public TextView tvTitle;
            public TextView tvLanguage;
            public TextView tvCountCards;

            public ViewHolder(View itemView) {
                super(itemView);
                root = itemView;
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvLanguage = (TextView) itemView.findViewById(R.id.tv_language);
                tvCountCards = (TextView) itemView.findViewById(R.id.tv_count_cards);
            }
        }

        public StacksAdapter(@Nullable Cursor query) {
            super(query);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stack, parent, false);
            return new ViewHolder(item);
        }

        private String shortenLanguage(@NonNull String language) {
            if (language.length() < 2) {
                return "";
            }
            if (language.length() == 2) {
                return String.valueOf(Character.toUpperCase(language.charAt(0))) + Character.toLowerCase(language.charAt(1));
            }
            return Character.toUpperCase(language.charAt(0)) + language.substring(1, 3).toLowerCase();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            query.moveToPosition(position);
            final long id = query.getLong(StacksQuery.ID);
            final String title = query.getString(StacksQuery.TITLE);
            final String language = query.getString(StacksQuery.LANGUAGE);
            final int count = query.getInt(StacksQuery.COUNT_CARDS);
            final int color = query.getInt(StacksQuery.COLOR);
            holder.tvTitle.setText(title);
            holder.tvLanguage.setText(shortenLanguage(language));
            holder.ivIcon.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            holder.tvCountCards.setText(String.valueOf(count));
            if (0 == count) {
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(getView(), getString(R.string.no_cards), Snackbar.LENGTH_SHORT)
                                .setAction(R.string.add_card, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), AddCardActivity.class);
                                        Uri data = ContentUris.withAppendedId(Stacks.CONTENT_URI, id);
                                        intent.setData(data);
                                        startActivity(intent);
                                    }
                                })
                                .setActionTextColor(getResources().getColor(R.color.snack_bar_positive))
                                .show();
                    }
                });
            } else {
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), TrainingActivity.class);
                        Uri data = ContentUris.withAppendedId(Stacks.CONTENT_URI, id);
                        data = UriUtils.insertParameter(data, Stacks.STACK_TITLE, title);
                        intent.setData(data);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    // UI elements.
    private RecyclerView rvStacks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_pick_stack, container, false);
        rvStacks = (RecyclerView) view.findViewById(R.id.rv_stacks);
        rvStacks.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false));
        getActivity().setTitle(getString(R.string.choose_stack));
        rvStacks.setAdapter(new StacksAdapter(null));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(StacksQuery._TOKEN, null, this);
    }

    private interface StacksQuery {
        int _TOKEN = 0;

        String[] COLUMNS = {
                Stacks.STACK_ID,
                Stacks.STACK_TITLE,
                Stacks.STACK_LANGUAGE,
                Stacks.STACK_COUNT_CARDS,
                Stacks.STACK_COLOR
        };

        int ID = 0;
        int TITLE = 1;
        int LANGUAGE = 2;
        int COUNT_CARDS = 3;
        int COLOR = 4;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Stacks.CONTENT_URI, StacksQuery.COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor query) {
        if (null == query) {
            throw new IllegalArgumentException("Loader was failed. (query = null)");
        }
        if (1 == query.getCount()) {
            query.moveToFirst();
            long id = query.getLong(StacksQuery.ID);
            Intent intent = new Intent(getActivity(), AddCardActivity.class);
            Uri data = ContentUris.withAppendedId(Stacks.CONTENT_URI, id);
            intent.setData(data);
            startActivity(intent);
        } else {
            ((StacksAdapter) rvStacks.getAdapter()).setCursor(query);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
