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

package com.imgtec.hobbyist.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Class contains commands that can be sent to the board.
 */
public enum Command {

  SETLED1("SET LED #1", "SET LED #1"),
  SETLED2("SET LED #2", "SET LED #2"),
  SETLED3("SET LED #3", "SET LED #3"),
  SETLED4("SET LED #4", "SET LED #4"),
  SAY_HELLO("SAY HELLO", "SAY HELLO"),
  GET_STATUS("GET STATUS", "GET STATUS"),
  REBOOT("REBOOT", "REBOOT"),
  FACTORY_RESET("FACTORY RESET", ""),
  RENAME_DEVICE("RENAME DEVICE", "");

  public static String prepareCommand(String display) {
    return display;
  }

  private String command;
  private String displayedAs;

  Command(String command, String displayedAs) {
    this.command = command;
    this.displayedAs = displayedAs;
  }

  public String getCommand() {
    return command;
  }

  public String getDisplayedAs() {
    return displayedAs;
  }

  public static List<String> getInteractiveModeCommands() {
    List<String> commands = new ArrayList<>();
    for (Command command : Command.values()) {
      if (!command.getDisplayedAs().equals("")) {
        commands.add(command.getDisplayedAs());
      }
    }
    return commands;
  }
}
