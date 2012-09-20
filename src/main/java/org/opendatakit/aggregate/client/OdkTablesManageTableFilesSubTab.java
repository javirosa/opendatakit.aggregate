package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesViewTable;
import org.opendatakit.aggregate.client.table.OdkTablesViewTableFileInfo;
import org.opendatakit.aggregate.client.widgets.AggregateListBox;
import org.opendatakit.aggregate.client.widgets.OdkTablesDisplayDeletedRowsCheckBox;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.client.widgets.TableEntryClientListBox;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * This class builds the subtab that allows for viewing and managing the files
 * that are associated with ODKTables tables. <br>
 * The idea here is that you will have uploaded files to the table, like an html
 * file with information about how to display a list view for the data in your
 * table. And then you can come to this page to see which files are actually
 * associated with the table, as well as set the keys which will say which file
 * does what.
 * 
 * @author sudar.sam@gmail.com
 * 
 */
public class OdkTablesManageTableFilesSubTab extends AggregateSubTabBase {

  // this is the panel with the information and the dropdown box
  // that tells you to select a table
  private FlexTable selectTablePanel;

  // the string constants for adding a file
  private static final String ADD_FILE_TXT = "Add a table file";
  private static final String ADD_FILE_TOOLTIP_TXT = "Upload a file";
  private static final String ADD_FILE_BALLOON_TXT = "Upload a file to be associated with a specific table";
  private static final String ADD_FILE_BUTTON_TXT = "<img src=\"images/yellow_plus.png\" />"
      + ADD_FILE_TXT;

  // this is a button for adding a file to be associated with a table.
  private ServletPopupButton addFileButton;
  /**
   * This will be the box that lets you choose which of the tables you are going
   * to view.
   * 
   * @return
   */
  private ListBox tableBox;
  /**
   * This is the int in the list box that is selected.
   */
  private int selectedValue;

  private HorizontalPanel topPanel;

  // array list so that you can access with indices reliably
  private final ArrayList<TableEntryClient> currentTables;
  // the box that shows the data
  private OdkTablesViewTableFileInfo tableFileData;

  // the current table that is being displayed
  private TableEntryClient currentTable;

  /**
   * Sets up the View Table subtab.
   */
  public OdkTablesManageTableFilesSubTab() {

    addFileButton = new ServletPopupButton(ADD_FILE_BUTTON_TXT, ADD_FILE_TXT,
        UIConsts.TABLE_FILE_UPLOAD_SERVLET_ADDR, this, ADD_FILE_TOOLTIP_TXT, ADD_FILE_BALLOON_TXT);

    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    // displayDeleted = false;
    currentTable = null;

    // first construct a copy so you can build the list box before you
    // update it. This seems like bad style.
    currentTables = new ArrayList<TableEntryClient>();

    // set up the box so you can only select one and you provide both the
    // table name and ID.
    tableBox = new ListBox();
    // new TableEntryClientListBox(currentTables, false, false,
    // "Select a table to view.");
    tableBox.addChangeHandler(new ChangeHandler() {

      public void onChange(ChangeEvent event) {
        int selectedIndex = tableBox.getSelectedIndex();
        if (selectedIndex > 0) {
          // Call this to clear the contents while you are waiting on
          // the response from the server.
          tableFileData.updateDisplay(null);
          selectedValue = selectedIndex;
          updateContentsForSelectedTable();
        }
      }
    });

    // now populate the list.
    updateTableList();

    tableFileData = new OdkTablesViewTableFileInfo(this);

    selectTablePanel = new FlexTable();
    selectTablePanel.getElement().setId("select_table_panel");
    selectTablePanel.setHTML(0, 0, "<h2 id=\"table_name\"> Select a Table </h2>");
    selectTablePanel.setWidget(0, 1, tableBox);
    selectTablePanel.setWidget(1, 0, addFileButton);

    // deletedRowsCheckBox = new OdkTablesDisplayDeletedRowsCheckBox(this,
    // displayDeleted);
    // selectTablePanel.setWidget(0, 2, deletedRowsCheckBox);

    topPanel = new HorizontalPanel();
    topPanel.add(selectTablePanel);
    topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    add(topPanel);
    add(tableFileData);

  }

  private void updateTableList() {
    SecureGWT.getServerTableService()
        .getTables(new AsyncCallback<List<TableEntryClient>>() {

      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(List<TableEntryClient> tables) {
        AggregateUI.getUI().clearError();

        addTablesToListBox(tables);
        tableBox.setItemSelected(selectedValue, true);
        
        // This makes the server go crazy with requests.
        //AggregateUI.getUI().getTimer().refreshNow();

      }
    });
  }

  @Override
  public boolean canLeave() {
    // sure you can leave.
    return true;
  }

  /*
   * temporarily existed to display deleted rows public Boolean
   * getDisplayDeleted() { return displayDeleted; }
   * 
   * public void setDisplayDeleted(Boolean display) { this.displayDeleted =
   * display; }
   */

  /**
   * This should just update the table list.
   */
  // does so by calling other methods.
  @Override
  public void update() {
    updateTableList();
    // this causing trouble
    updateTableData();
  }

  public void addTablesToListBox(List<TableEntryClient> tables) {
    // clear the old tables
    currentTables.clear();
    // and add the new
    currentTables.addAll(tables);

    // now update the list box
    tableBox.clear();
    tableBox.addItem(""); // blank holder to start with no selection
    for (int i = 0; i < currentTables.size(); i++) {
      tableBox.addItem(currentTables.get(i).getTableName());
    }
  }

  public void updateContentsForSelectedTable() {
    // - 1 because you have an extra entry that is the "" holder so
    // that the listbox starts empty.
    if (this.selectedValue == 0) {
      // if they select 0, clear the table
      tableFileData.updateDisplay(null);
    } else {
      currentTable = currentTables.get(this.selectedValue - 1);
      tableFileData.updateDisplay(currentTable);

      selectTablePanel.setHTML(2, 0, "<h2 id=\"table_displayed\"> Displaying: </h2>");
      selectTablePanel.setHTML(2, 1, "<h2 id=\table_name\"> " + currentTable.getTableName()
          + " </h2>");
      add(tableFileData);
    }
  }

  public void updateTableData() {
    tableFileData.updateDisplay(currentTable);
  }

}
