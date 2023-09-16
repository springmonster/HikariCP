/*
 * Copyright (C) 2013 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.pool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is the proxy class for java.sql.Statement.
 *
 * @author Brett Wooldridge
 */
public abstract class ProxyStatement implements Statement {

   protected final ProxyConnection connection;
   final Statement delegate;

   private boolean isClosed;
   private ResultSet proxyResultSet;

   ProxyStatement(ProxyConnection connection, Statement statement) {
      this.connection = connection;
      this.delegate = statement;
   }

   final SQLException checkException(SQLException e) {
      return connection.checkException(e);
   }

   /** {@inheritDoc} */
   @Override
   public final String toString() {
      final var delegateToString = delegate.toString();
      return this.getClass().getSimpleName() + '@' + System.identityHashCode(this) + " wrapping "
         + delegateToString;
   }

   // **********************************************************************
   //                 Overridden java.sql.Statement Methods
   // **********************************************************************

   /** {@inheritDoc} */
   @Override
   public final void close() throws SQLException {
      synchronized (this) {
         if (isClosed) {
            return;
         }

         isClosed = true;
      }

      connection.untrackStatement(delegate);

      try {
         delegate.close();
      } catch (SQLException e) {
         throw connection.checkException(e);
      }
   }

   /** {@inheritDoc} */
   @Override
   public Connection getConnection() throws SQLException {
      return connection;
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.execute(sql);
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.execute(sql, autoGeneratedKeys);
   }

   /** {@inheritDoc} */
   @Override
   public ResultSet executeQuery(String sql) throws SQLException {
      connection.markCommitStateDirty();
      ResultSet resultSet = delegate.executeQuery(sql);
      return ProxyFactory.getProxyResultSet(connection, this, resultSet);
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeUpdate(sql);
   }

   /** {@inheritDoc} */
   @Override
   public int[] executeBatch() throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeBatch();
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeUpdate(sql, autoGeneratedKeys);
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeUpdate(sql, columnIndexes);
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeUpdate(sql, columnNames);
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.execute(sql, columnIndexes);
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql, String[] columnNames) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.execute(sql, columnNames);
   }

   /** {@inheritDoc} */
   @Override
   public long[] executeLargeBatch() throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeLargeBatch();
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeLargeUpdate(sql);
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeLargeUpdate(sql, autoGeneratedKeys);
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeLargeUpdate(sql, columnIndexes);
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
      connection.markCommitStateDirty();
      return delegate.executeLargeUpdate(sql, columnNames);
   }

   /** {@inheritDoc} */
   @Override
   public ResultSet getResultSet() throws SQLException {
      final var resultSet = delegate.getResultSet();
      if (resultSet != null) {
         if (proxyResultSet == null || ((ProxyResultSet) proxyResultSet).delegate != resultSet) {
            proxyResultSet = ProxyFactory.getProxyResultSet(connection, this, resultSet);
         }
      } else {
         proxyResultSet = null;
      }
      return proxyResultSet;
   }

   /** {@inheritDoc} */
   @Override
   public ResultSet getGeneratedKeys() throws SQLException {
      var resultSet = delegate.getGeneratedKeys();
      if (proxyResultSet == null || ((ProxyResultSet) proxyResultSet).delegate != resultSet) {
         proxyResultSet = ProxyFactory.getProxyResultSet(connection, this, resultSet);
      }
      return proxyResultSet;
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("unchecked")
   public final <T> T unwrap(Class<T> iface) throws SQLException {
      if (iface.isInstance(delegate)) {
         return (T) delegate;
      } else if (delegate != null) {
         return delegate.unwrap(iface);
      }

      throw new SQLException("Wrapped statement is not an instance of " + iface);
   }
}
