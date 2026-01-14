package org.java.learn.summary.java.framework.struct.composite.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duyanlong on 2019/6/20.
 * Composite.
 */
public class ProjectManager extends Employ {

    public ProjectManager(){
        super.employs = new ArrayList<>();
    }

    @Override
    public void add(Employ employ) {
        super.employs.add(employ);
    }

    @Override
    public void delete(Employ employ) {
        employs.remove(employ);
    }

    public List<Employ> getEmploys(){
        return employs;
    }
}
