package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class CopyFilterGroupButton extends AButtonBase implements ClickHandler {

  private FilterSubTab parentSubTab;
  
  public CopyFilterGroupButton(FilterSubTab parentSubTab) {
    super("Copy");
    this.parentSubTab = parentSubTab;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    FilterGroup filterGroup = parentSubTab.getDisplayedFilterGroup();
    
    List<Filter> newFilterGroupfilters = new ArrayList<Filter>();
    newFilterGroupfilters.addAll(filterGroup.getFilters());
    FilterGroup newGroup = new FilterGroup(UIConsts.FILTER_NONE, filterGroup.getFormId(), newFilterGroupfilters);

    // set the displaying filters to the newly saved filter group
    parentSubTab.switchFilterGroupWithinForm(newGroup);
    AggregateUI.getUI().getTimer().refreshNow();
  }
}
