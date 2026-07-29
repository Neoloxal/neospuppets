package com.neoloxal.neospuppets.puppets;

import net.minecraft.util.StringRepresentable;

public enum Skin implements StringRepresentable {
    PUPPET("puppet"),
    NEOLOXAL("neoloxal");
    
    private final String NAME;

    Skin(String name) {
        this.NAME = name;
    }

    @Override
    public String getSerializedName() {
        return this.NAME;
    }
}
