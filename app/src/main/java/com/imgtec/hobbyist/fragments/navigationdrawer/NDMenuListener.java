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

package com.imgtec.hobbyist.fragments.navigationdrawer;

import android.support.v4.app.Fragment;

import com.imgtec.hobbyist.utils.NDMenuMode;

public interface NDMenuListener {

  void onSelectionAndTitleChange(NDMenuItem menuItem);

  void onTitleChange(String titleId);

  void onFragmentChange(Fragment fragment);

  void onFragmentChangeWithBackstackClear(Fragment fragment);

  void onMenuChange(NDMenuMode mode);

}
