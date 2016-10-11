package seedu.tasklist.logic.parser;

import static seedu.tasklist.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tasklist.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.tasklist.commons.exceptions.IllegalValueException;
import seedu.tasklist.commons.util.StringUtil;
import seedu.tasklist.logic.commands.*;
import seedu.tasklist.model.task.EndTime;
import seedu.tasklist.model.task.Priority;
import seedu.tasklist.model.task.StartTime;
import seedu.tasklist.model.task.TaskDetails;

/**
 * Parses user input.
 */
public class Parser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    private static final Pattern TASK_INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    private static final Pattern KEYWORDS_ARGS_FORMAT = Pattern.compile("(?<keywords>\\S+(?:\\s+\\S+)*)");

    private static final Pattern TASK_DATA_ARGS_FORMAT = Pattern.compile(
            "(?<name>([^/](?<!(at|from|to|by) ))*)" + "((?: (at|from) )(?<start>(([^/](?<! (to|by) ))|(\\[^/]))+))?"
                    + "((?: (to|by) )(?<end>(([^/](?<! p/))|(\\[^/]))+))?" + "((?: p/)(?<priority>[^/]+))?"
                    + "(?<tagArguments>(?: t/[^/]+)*)"
                    );
    
    private static final Pattern TASK_UPDATE_ARGS_FORMAT = Pattern.compile(
            "((?<name>([^/](?<!(at|from|to|by) ))*))?" + "((?: (at|from) )(?<start>(([^/](?<! (to|by) ))|(\\[^/]))+))?"
                    + "((?: (to|by) )(?<end>(([^/](?<! p/))|(\\[^/]))+))?" + "((?: p/)(?<priority>[^/]+))?"
                    + "(?<tagArguments>(?: t/[^/]+)*)"
                    );
    
/*
    private static final Pattern UPDATE_COMPLETE_ARGS_PARSER = Pattern.compile(
            "(?<targetIndex>(\\d&&\\S)+)"
            + "(?<name>([^/](?<! (at|from|to|by) ))+)" + "((?: (at|from) )(?<start>(([^/](?<! (to|by) ))|(\\[^/]))+))?"
            + "((?: (to|by) )(?<end>(([^/](?<! p/))|(\\[^/]))+))?" + "((?: p/)(?<priority>[^/]+))?"
            + "(?<tagArguments>(?: t/[^/]+)*)"
            );
*/
    private static final Pattern UPDATE_COMPLETE_ARGS_PARSER = Pattern.compile(
    "(?<targetIndex>(\\d&&\\S)+)"+
    "(?<name>([^/](?<!(at|from|to|by) ))*)" + "((?: (at|from) )(?<start>(([^/](?<! (to|by) ))|(\\[^/]))+))?"
    + "((?: (to|by) )(?<end>(([^/](?<! p/))|(\\[^/]))+))?" + "((?: p/)(?<priority>[^/]+))?"
    + "(?<tagArguments>(?: t/[^/]+)*)"
    );
    private static final Pattern DELETE_COMPLETE_ARGS_PARSER = Pattern
            .compile("(?<index>(\\d+)?)|" + "(?<searchString>[^/]+)");

    public Parser() {
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
        switch (commandWord) {

        case AddCommand.COMMAND_WORD:
            return prepareAdd(arguments);

        case DoneCommand.COMMAND_WORD:
            return prepareDone(arguments);

        case DeleteCommand.COMMAND_WORD:
            return prepareDelete(arguments);

        case UpdateCommand.COMMAND_WORD:
            return prepareUpdate(arguments);

        case ClearCommand.COMMAND_WORD:
            return new ClearCommand();

        case FindCommand.COMMAND_WORD:
            return prepareFind(arguments);

        case ExitCommand.COMMAND_WORD:
            return new ExitCommand();

        case HelpCommand.COMMAND_WORD:
            return new HelpCommand();

        default:
            return new IncorrectCommand(MESSAGE_UNKNOWN_COMMAND);
        }
    }

    private Command prepareUpdate(String args) {
        args = args.trim();
        int targetIndex = getIndex(args);
        args = removeIndex(args);
        System.out.println(args + " | " + targetIndex);
        final Matcher matcher = TASK_UPDATE_ARGS_FORMAT.matcher(args);
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UpdateCommand.MESSAGE_USAGE));
        } else {
//          int targetIndex;
            String taskDetails;
            String startTime;
            // String startDate;
            String endTime;
            // String endDate;
            String priority;
//            targetIndex = Integer.valueOf(matcher.group("targetIndex"));
            taskDetails = (matcher.group("name") == null) ? null : matcher.group("name");
            // startDate = ; - need to modify regex
            startTime = (matcher.group("start") == null) ? null : matcher.group("start");
            // endDate = ; - need to modify regex
            endTime = (matcher.group("end") == null) ? null : matcher.group("end");
            priority = (matcher.group("priority") == null) ? null : matcher.group("priority");
 /*
            System.out.println(targetIndex);
            System.out.println(taskDetails);
            System.out.println(startTime);
            System.out.println(endTime);
            System.out.println(priority);
  */
            try {
                return new UpdateCommand(targetIndex, taskDetails, startTime,
                        // startDate,
                        endTime,
                        // endDate,
                        priority);
            } catch (IllegalValueException ive) {
                return new IncorrectCommand(ive.getMessage());
            }
        }

    }

    private String removeIndex(String args) {
        // TODO Auto-generated method stub
        args = args.replace(Integer.toString(getIndex(args)), "");
        args = args.trim();
        return args;
    }

    private int getIndex(String args) {
        String index="";
        for(int i=0; i<args.length(); i++){
            if(args.charAt(i) >= '0' && args.charAt(i) <= '9')
                index = index + args.charAt(i);
            else break;
        }
        return Integer.parseInt(index);
    }

    private Command prepareDone(String args) {
        final Matcher matcher = DELETE_COMPLETE_ARGS_PARSER.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        } else if (matcher.group("index") != null) {
            return new DoneCommand(Integer.valueOf(matcher.group("index")));
        } else if (matcher.group("searchString") != null) {
            return new DoneCommand('*' + matcher.group("searchString") + '*');
        }
        return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

    /**
     * Parses arguments in the context of the add person command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareAdd(String args) {
        final Matcher taskMatcher = TASK_DATA_ARGS_FORMAT.matcher(args.trim());

        if (!taskMatcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        } else {
            String startTime = (taskMatcher.group("start") == null) ? "" : taskMatcher.group("start");
            String endTime = (taskMatcher.group("end") == null) ? "" : taskMatcher.group("end");
            try {
                return new AddCommand(taskMatcher.group("name").replace("\\", ""), startTime, endTime,
                        taskMatcher.group("priority"), getTagsFromArgs(taskMatcher.group("tagArguments")));
            } catch (IllegalValueException ive) {
                return new IncorrectCommand(ive.getMessage());
            }
        }
    }

    /**
     * Extracts the new person's tags from the add command's tag arguments
     * string. Merges duplicate tag strings.
     */
    private static Set<String> getTagsFromArgs(String tagArguments) throws IllegalValueException {
        // no tags
        if (tagArguments.isEmpty()) {
            return Collections.emptySet();
        }
        // replace first delimiter prefix, then split
        final Collection<String> tagStrings = Arrays.asList(tagArguments.replaceFirst(" t/", "").split(" t/"));
        return new HashSet<>(tagStrings);
    }

    /**
     * Parses arguments in the context of the delete person command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareDelete(String args) {
        final Matcher matcher = DELETE_COMPLETE_ARGS_PARSER.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        } else if (matcher.group("index") != null) {
            return new DeleteCommand(Integer.valueOf(matcher.group("index")));
        } else if (matcher.group("searchString") != null) {
            return new DeleteCommand('*' + matcher.group("searchString") + '*');
        }
        return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

    /**
     * Returns the specified index in the {@code command} IF a positive unsigned
     * integer is given as the index. Returns an {@code Optional.empty()}
     * otherwise.
     */
    private Optional<Integer> parseIndex(String command) {
        final Matcher matcher = TASK_INDEX_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String index = matcher.group("targetIndex");
        if (!StringUtil.isUnsignedInteger(index)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(index));

    }

    /**
     * Parses arguments in the context of the find person command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareFind(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        /*
         * // keywords delimited by whitespace final String[] keywords =
         * matcher.group("keywords").split("\\s+"); final Set<String> keywordSet
         * = new HashSet<>(Arrays.asList(keywords)); return new
         * FindCommand(keywordSet);
         */
        args = args.trim();
        args = '*' + args + '*';
        return new FindCommand(args);
    }

}