package io.bclub.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {

    int verticalSpacing = 0, horizontalSpacing = 0;

    public SpacingItemDecoration(int verticalSpacing, int horizontalSpacing) {
        this.verticalSpacing = verticalSpacing;
        this.horizontalSpacing = horizontalSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);

        if (holder.getLayoutPosition() != 0) {
            outRect.set(horizontalSpacing, verticalSpacing, horizontalSpacing, verticalSpacing);
        } else {
            outRect.set(horizontalSpacing, 0, horizontalSpacing, verticalSpacing);
        }
    }
}
