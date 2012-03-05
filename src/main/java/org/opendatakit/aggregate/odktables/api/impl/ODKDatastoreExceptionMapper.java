package org.opendatakit.aggregate.odktables.api.impl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;

@Provider
public class ODKDatastoreExceptionMapper implements ExceptionMapper<ODKDatastoreException> {

  @Override
  public Response toResponse(ODKDatastoreException e) {
    if (e instanceof ODKEntityNotFoundException) {
      return Response.status(Status.NOT_FOUND).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
          .build();
    } else if (e instanceof ODKEntityPersistException) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Could not save: " + e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    } else if (e instanceof ODKOverQuotaException) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Over quota: " + e.getMessage())
          .type(MediaType.TEXT_PLAIN).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN).build();
    }
  }

}
