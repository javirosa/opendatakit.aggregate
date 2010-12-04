/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence;

import java.util.List;
import java.util.Set;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * The Query interface defines how persistence implementations should create query functionality.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public interface Query {
  
	/**
	 * This enum defines the directions which the Query can be sorted on.
	 *
	 */
  public enum Direction {
    ASCENDING,
    DESCENDING;
  };
  
  /**
   * This enum defines the different filter operations which the Query can have.
   *
   */
  public enum FilterOperation {
   EQUAL,
   GREATER_THAN,
   GREATER_THAN_OR_EQUAL,
   LESS_THAN,
   LESS_THAN_OR_EQUAL;
  };
  
  /**
   * Adds a sort to the query using the given attribute name and sort direction.
   * 
   * @param attributeName the name of the attribute to sort on
   * @param direction a Query.Direction which defines the direction to sort
   */
  public void addSort(DataField attributeName, Direction direction);
  
  /**
   * Adds a filter to the query using the given attribute name, filter operation, and value.
   * 
   * @param attributeName the name of the attribute to filter
   * @param op the Query.FilterOperation to use for filtering
   * @param value the value to filter with
   */
  public void addFilter(DataField attributeName, FilterOperation op, Object value);
  
  /**
   * Adds a filter to the query using the given attribute name, filtered by the values
   * IN the value set.
   * 
   * @param attributeName
   * @param valueSet
   */
  public void addValueSetFilter(DataField attributeName, Set<?> valueSet );
  
  /**
   * Returns a list of entities which are the results of executing the query.
   * 
   * @param fetchLimit the maximum number of Entity objects to retrieve from the Datastore
   * @return a List<Entity> which contains the Entity objects from the results of the Query
   * @throws ODKDatastoreException if there was a  problem executing the Query
   */
  public List<? extends CommonFieldsBase> executeQuery(int fetchLimit) throws ODKDatastoreException;

  /**
   * Returns a list of distinct EntityKeys of the topLevelAuri for the set of records
   * returned by the query.
   * 
   * @param topLevelTable definition of the relation that the topLevelAuri corresponds to.
   * @param fetchLimit
   * @return
   * @throws ODKDatastoreException
   */
  public Set<EntityKey> executeTopLevelKeyQuery(CommonFieldsBase topLevelTable,
		  						int fetchLimit) throws ODKDatastoreException;


  /**
   * Returns the list of distinct values for a given field with any given filter
   * and sort criteria.
   * 
   * @param dataField
   * @return
 * @throws ODKDatastoreException 
   */
  public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException;
}
