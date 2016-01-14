/*  
 *  $ Id: $
 *  Copyright © 2016 Experian Ltd. All rights reserved.
 *  No Part of this file may be copied or distributed without the permission of Experian Ltd.
 */

package primenumbers.core;

import java.util.List;

public interface CheckerSelectionStrategy {

    PrimeNumberChecker selectChecker(List<PrimeNumberChecker> checkers);
}
