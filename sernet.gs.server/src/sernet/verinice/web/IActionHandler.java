package sernet.verinice.web;

import java.io.Serializable;

public interface IActionHandler extends Serializable{
 
    void execute();
    
    String getLabel();
    
    void setLabel(String label);
}
