package cn.tealc.ntemaid.service;

import java.util.Optional;

public class NativeProcessService {
    public void killOfficialLauncher(){
        killProcessByName("NTEGame.exe",false);
    }



    /**
     * 判断指定名称的程序是否正在运行
     *
     * @param processName 进程名称
     * @return 如果正在运行返回该进程的 ProcessHandle，否则返回 Optional.empty()
     */
    public Optional<ProcessHandle> findProcessByName(String processName) {
        if (processName == null || processName.isEmpty()) {
            return Optional.empty();
        }

        // ProcessHandle.allProcesses() 会返回当前系统所有的进程流 (Stream<ProcessHandle>)
        return ProcessHandle.allProcesses()
                .filter(ph -> ph.info().command().isPresent()) // 过滤失掉没有命令路径的进程（比如一些系统内核空闲进程）
                .filter(ph -> {
                    String command = ph.info().command().get();
                    // 检查路径是否以目标进程名结尾（兼容绝对路径或纯文件名）
                    return command.equalsIgnoreCase(processName)
                            || command.endsWith("\\" + processName)
                            || command.endsWith("/" + processName);
                })
                .findFirst(); // 找到第一个匹配的
    }

    /**
     * 根据进程名称直接结束运行
     *
     * @param processName 进程名称
     * @param force 是否强制结束 (true 相当于 kill -9 或 TerminateProcess，false 则是正常优雅关闭)
     * @return 是否成功发出关闭指令
     */
    public boolean killProcessByName(String processName, boolean force) {
        Optional<ProcessHandle> processOpt = findProcessByName(processName);

        if (processOpt.isPresent()) {
            ProcessHandle ph = processOpt.get();
            System.out.println("找到进程 [" + processName + "], PID: " + ph.pid());

            if (force) {
                // 强行终止进程
                return ph.destroyForcibly();
            } else {
                // 正常请求关闭进程
                return ph.destroy();
            }
        }

        System.out.println("程序 [" + processName + "] 未在运行。");
        return false;
    }
}
