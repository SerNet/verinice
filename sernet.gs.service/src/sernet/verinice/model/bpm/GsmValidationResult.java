package sernet.verinice.model.bpm;

import java.util.LinkedList;
import java.util.List;

import sernet.verinice.interfaces.bpm.IGsmValidationResult;

@SuppressWarnings("serial")
public class GsmValidationResult implements IGsmValidationResult {

    private List<String> ungroupedAssets;
    
    private List<String> ungroupedControls;
    
    private List<String> assetGroupNamesWithoutLinkedPerson;
    
    private int numberOfRelevantAssetGroups;

    public GsmValidationResult() {
        super();
        numberOfRelevantAssetGroups = 0;
        ungroupedAssets = new LinkedList<String>();
        ungroupedControls = new LinkedList<String>();
        assetGroupNamesWithoutLinkedPerson = new LinkedList<String>();
    }
    
    @Override
    public void addUngroupedAsset(String title) {
        ungroupedAssets.add(title);
    }
    
    public List<String> getUngroupedAssets() {
        return ungroupedAssets;
    }

    @Override
    public void addUngroupedControl(String title) {
        ungroupedControls.add(title);
    }

    public List<String> getUngroupedControls() {
        return ungroupedControls;
    }

    @Override
    public List<String> getAssetGroupsWithoutLinkedPerson() {
        return assetGroupNamesWithoutLinkedPerson;
    }

    @Override
    public void addAssetGroupWithoutLinkedPerson(String title) {
        assetGroupNamesWithoutLinkedPerson.add(title);    
    }
    
    @Override
    public int getNumberOfRelevantAssetGroups() {
        return this.numberOfRelevantAssetGroups;
    }
    
    public void setNumberOfRelevantAssetGroups(int n) {
        this.numberOfRelevantAssetGroups = n;
    }

    @Override
    public void oneMoreRelevantAssetGroup() {
        numberOfRelevantAssetGroups++;
    }
 
}
