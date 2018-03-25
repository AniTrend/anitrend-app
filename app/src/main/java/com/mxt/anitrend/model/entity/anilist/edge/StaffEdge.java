package com.mxt.anitrend.model.entity.anilist.edge;

import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.attribute.Edge;

/**
 * Created by max on 2018/03/25.
 * StaffEdge
 */

public class StaffEdge extends Edge<StaffBase> {

    private String role;

    /**
     * The role of the staff member in the production of the media
     */
    public String getRole() {
        return role;
    }
}
