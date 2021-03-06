/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.connector;

import io.prestosql.spi.security.PrestoPrincipal;
import io.prestosql.spi.security.Privilege;

import java.util.Set;

public interface Grants<T>
{
    void grant(PrestoPrincipal principal, T objectName, Set<Privilege> privileges, boolean grantOption);

    void revoke(PrestoPrincipal principal, T objectName, Set<Privilege> privileges, boolean grantOption);

    boolean isAllowed(String user, T objectName, Privilege privilege);

    boolean canGrant(String user, T objectName, Privilege privilege);
}
