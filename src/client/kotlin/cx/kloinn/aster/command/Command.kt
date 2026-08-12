package cx.kloinn.aster.command

abstract class Command {
    abstract val names: List<String>
    abstract val description: String
    abstract val helpMessage: String?

    abstract fun runCommand(args: List<String>)
}