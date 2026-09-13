package moze_intel.projecte;

import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Who may run ProjectE's commands.
 * <p>
 * NeoForge had a permission node system that server owners could point at a permissions mod to grant commands
 * per player or group. Fabric has no equivalent in either the loader or Fabric API, so this falls back to
 * vanilla operator levels, which is what NeoForge's own defaults resolved to anyway. The practical loss is that
 * a server owner can no longer hand out individual ProjectE commands without opping someone.
 * <p>
 * The node names are kept so the permission strings stay stable if a permissions API is wired up later.
 */
public class PEPermissions {

	//Commands
	public static final CommandPermissionNode COMMAND = node("command", Commands.LEVEL_ALL);

	public static final CommandPermissionNode COMMAND_REMOVE_EMC = nodeOpCommand("remove_emc");
	public static final CommandPermissionNode COMMAND_RESET_EMC = nodeOpCommand("reset_emc");
	public static final CommandPermissionNode COMMAND_SET_EMC = nodeOpCommand("set_emc");
	public static final CommandPermissionNode COMMAND_SHOW_BAG = nodeOpCommand("show_bag");
	public static final CommandPermissionNode COMMAND_EMC = nodeOpCommand("emc");
	public static final CommandPermissionNode COMMAND_EMC_ADD = nodeSubCommand(COMMAND_EMC, "add");
	public static final CommandPermissionNode COMMAND_EMC_REMOVE = nodeSubCommand(COMMAND_EMC, "remove");
	public static final CommandPermissionNode COMMAND_EMC_SET = nodeSubCommand(COMMAND_EMC, "set");
	public static final CommandPermissionNode COMMAND_EMC_TEST = nodeSubCommand(COMMAND_EMC, "test");
	public static final CommandPermissionNode COMMAND_EMC_GET = nodeSubCommand(COMMAND_EMC, "get");
	public static final CommandPermissionNode COMMAND_KNOWLEDGE = nodeOpCommand("knowledge");
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_CLEAR = nodeSubCommand(COMMAND_KNOWLEDGE, "clear");
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_LEARN = nodeSubCommand(COMMAND_KNOWLEDGE, "learn");
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_UNLEARN = nodeSubCommand(COMMAND_KNOWLEDGE, "unlearn");
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_TEST = nodeSubCommand(COMMAND_KNOWLEDGE, "test");

	private PEPermissions() {
	}

	private static CommandPermissionNode node(String nodeName, int requiredLevel) {
		return new CommandPermissionNode(PECore.MODID + "." + nodeName, requiredLevel);
	}

	private static CommandPermissionNode nodeOpCommand(String nodeName) {
		return node("command." + nodeName, Commands.LEVEL_GAMEMASTERS);
	}

	/**
	 * A sub command is only reached once its parent has already been allowed, so it inherits the parent's level.
	 */
	private static CommandPermissionNode nodeSubCommand(CommandPermissionNode parent, String nodeName) {
		return new CommandPermissionNode(parent.nodeName() + "." + nodeName, parent.requiredLevel());
	}

	/**
	 * @param nodeName      The permission this command would be gated on, were a permissions API available.
	 * @param requiredLevel The operator level required to run it.
	 */
	public record CommandPermissionNode(String nodeName, int requiredLevel) implements Predicate<CommandSourceStack> {

		@Override
		public boolean test(CommandSourceStack source) {
			return source.hasPermission(requiredLevel);
		}
	}
}
