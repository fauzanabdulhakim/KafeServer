package com.fauzan.kafeserver.model.EventBus;

import com.fauzan.kafeserver.model.AddonModel;

import java.util.List;

public class UpdateAddonModel {
    private List<AddonModel> addonModel;

    public UpdateAddonModel() {

    }

    public List<AddonModel> getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }
}
