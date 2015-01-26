/*
 * <b>Copyright 2014 by Imagination Technologies Limited
 * and/or its affiliated group companies.</b>\n
 * All rights reserved.  No part of this software, either
 * material or conceptual may be copied or distributed,
 * transmitted, transcribed, stored in a retrieval system
 * or translated into any human or computer language in any
 * form by any means, electronic, mechanical, manual or
 * other-wise, or disclosed to the third parties without the
 * express written permission of Imagination Technologies
 * Limited, Home Park Estate, Kings Langley, Hertfordshire,
 * WD4 8LZ, U.K.
 */

package com.imgtec.hobbyist.utils;

/**
 * Class to be used with {@link com.imgtec.hobbyist.utils.BackgroundExecutor}.
 * Implementations should provide operations to perform in the BackgroundExecutor's submit() method.
 * This operation must return some result to the caller.
 *
 * @param <RESULT>
 */
public interface ExternalCall<RESULT> {
  RESULT submit();
}
