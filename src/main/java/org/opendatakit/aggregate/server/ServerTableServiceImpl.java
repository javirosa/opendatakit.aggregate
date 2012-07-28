package org.opendatakit.aggregate.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.ServerTableService;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.impl.api.TableServiceImpl;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.engine.gae.DatastoreImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerTableServiceImpl extends RemoteServiceServlet implements
		ServerTableService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3291707708959185034L;
	private static final Log logger = LogFactory.getLog(TableServiceImpl.class);

	@Override
	public List<TableResourceClient> getTables(UriInfo info)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    TableManager tm = new TableManager(cc);
	    try {
		    List<Scope> scopes = AuthFilter.getScopes(cc);
		    List<TableEntry> entries = tm.getTables(scopes);
		    ArrayList<TableResourceClient> resources = new ArrayList<TableResourceClient>();
		    for (TableEntry entry : entries) {
		      resources.add(getResource(entry, info));
		    }
		    return resources;
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}

	@Override
	public TableResourceClient getTable(String tableId, UriInfo info)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    TableManager tm = new TableManager(cc);
	    try {
		    new AuthFilter(tableId, cc).checkPermission(TablePermission.READ_TABLE_ENTRY);
		    TableEntry entry = tm.getTableNullSafe(tableId);
		    TableResourceClient resource = getResource(entry, info);
		    return resource;
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}

	@Override
	public TableResourceClient createTable(String tableId,
			TableDefinitionClient definition, UriInfo info)
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
			PermissionDeniedException, TableAlreadyExistsException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    TableManager tm = new TableManager(cc);		
	    // TODO: add access control stuff
	    // Have to be careful of all the transforms going on here. 
	    // Make sure they actually work as expected!
	    // also have to be sure that I am passing in an actual colum and not a 
	    // column resource or something, in which case the transform() method is not
	    // altering all of the requisite fields.
	    try {
		    String tableName = definition.getTableName();
		    List<ColumnClient> columns = definition.getColumns();
		    List<Column> columnsServer = new ArrayList<Column>();
		    for (ColumnClient column : columns) {
		    	columnsServer.add(column.transform());
		    }
		    String metadata = definition.getMetadata();
		    TableEntry entry = tm.createTable(tableId, tableName, columnsServer, metadata);
		    TableResourceClient resource = getResource(entry, info);
		    logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
		    return resource;
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}

	@Override
	public void deleteTable(String tableId, UriInfo info)
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
			PermissionDeniedException, ODKTaskLockException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    TableManager tm = new TableManager(cc);	
	    try {
		    new AuthFilter(tableId, cc).checkPermission(TablePermission.DELETE_TABLE);
		    tm.deleteTable(tableId);
		    logger.info("tableId: " + tableId);
		    DatastoreImpl ds = (DatastoreImpl) cc.getDatastore();
		    ds.getDam().logUsage();
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}
	
	  private TableResourceClient getResource(TableEntry entry, UriInfo info) {
		    String tableId = entry.getTableId();

		    UriBuilder ub = info.getBaseUriBuilder();
		    ub.path(TableService.class);
		    URI self = ub.clone().path(TableService.class, "getTable").build(tableId);
		    URI properties = ub.clone().path(TableService.class, "getProperties").build(tableId);
		    URI data = ub.clone().path(TableService.class, "getData").build(tableId);
		    URI diff = ub.clone().path(TableService.class, "getDiff").build(tableId);
		    URI acl = ub.clone().path(TableService.class, "getAcl").build(tableId);

		    TableResource resource = new TableResource(entry);
		    resource.setSelfUri(self.toASCIIString());
		    resource.setPropertiesUri(properties.toASCIIString());
		    resource.setDataUri(data.toASCIIString());
		    resource.setDiffUri(diff.toASCIIString());
		    resource.setAclUri(acl.toASCIIString());
		    return resource.transform();
		  }	

}
