package linWanCheng.gitTool;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 批量获取Git某个文件或某行的提交统计信息
 *
 * @author 林万程
 */
public class GitTool {
    private static final Logger loggger = LoggerFactory.getLogger(GitTool.class);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static final String FILE_PATH = "filePath";
    public static final String ERROR_MESSAGE = "errorMessage";

    // blame

    public static final String BLAME_LINE = "blameLine";
    public static final String BLAME_NAME = "blameName";
    public static final String BLAME_TIME = "blameTime";
    public static final String NAME_LINE_COUNT = "nameLineCount";
    public static final String MORE_LINE_NAME = "moreLineName";

    // log

    public static final String NO_IGNORE_MERGE = "noIgnoreMerge";
    public static final String LAST_NAME = "lastName";
    public static final String LAST_TIME = "lastTime";
    public static final String NAME_COMMIT_COUNT = "nameCommitCount";
    public static final String MORE_COMMIT_NAME = "moreCommitName";

    public String gitPath;
    public Git git;

    public boolean init(String gitPath) {
        try {
            gitPath = gitPath.trim();
            // can add ".git"
            int index = gitPath.indexOf(".git");
            if (index > 0) {
                gitPath = gitPath.substring(0, gitPath.length() - ".git".length());
            }
            File gitDir = new File(gitPath, ".git");
            if (!"".equals(gitPath) && gitDir.exists()) {
                this.gitPath = new File(gitPath).getAbsolutePath();
                git = Git.init().setGitDir(gitDir).call();
                return true;
            }
        } catch (Exception e) {
            loggger.error("init false", e);
        }
        return false;
    }

    /**
     * @param multiPathAndLine MultiLine filePath:line split <code>"\n"</code><br/>
     * @param param            <code>Set&lt;String&gt;</code> like
     *                         <strong><code>GitTool.NO_IGNORE_MERGE<code/></strong> and return key
     * @return a <code>List&lt;LinkedHashMap&lt;String, String&gt;&gt;</code> for logInfo
     * @see linWanCheng.gitTool.GitTool#logInfo(String, Set)
     */
    public List<Map<String, String>> logInfoMultiLine(final String multiPathAndLine, final Set<String> param) {
        List<Map<String, String>> list = new ArrayList<>();
        String[] files = {multiPathAndLine};
        if (multiPathAndLine.contains("\n")) {
            files = multiPathAndLine.split("\n");
        }
        for (int i = 0; i < files.length; i++) {
            try {
                // GitAPIException
                list.add(logInfo(files[i], param));
            } catch (Exception e) {
                loggger.error(i + " " + files[i], e);
                Map<String, String> logInfoMap = new LinkedHashMap<>();
                logInfoMap.put(ERROR_MESSAGE, e.getLocalizedMessage());
                list.add(logInfoMap);
            }
        }
        return list;
    }

    // region logInfo(filePath, line, param)

    /**
     * get log info to find who is responsible for a file or a line
     *
     * @param pathAndLine filePath:LineNum<br/>
     *                    filePath can use "\" or "/", Absolute path or relative path of <code>".git"<code/><br/>
     *                    blame Line let return key have
     *                    <strong><code>BLAME_NAME<code/></strong> and
     *                    <strong><code>BLAME_TIME<code/></strong>
     * @param param       <code>Set&lt;String&gt;</code> like
     *                    <strong><code>GitTool.NO_IGNORE_MERGE<code/></strong> and return key
     * @return a <code>LinkedHashMap&lt;String, String&gt;</code> key in:
     * <strong><code>
     * <br/>BLAME_NAME
     * <br/>BLAME_TIME
     * <br/>NAME_LINE_COUNT
     * <br/>MORE_LINE_NAME
     * <br/>
     * <br/>LAST_NAME
     * <br/>LAST_TIME
     * <br/>NAME_COMMIT_COUNT
     * <br/>MORE_COMMIT_NAME
     * <code/></strong>
     * @throws GitAPIException is not git file
     */
    public Map<String, String> logInfo(final String pathAndLine, final Set<String> param) throws GitAPIException {
        Map<String, String> logInfoMap = new LinkedHashMap<>();
        String filePath = pathAndLine.trim();
        filePath = filePath.replaceAll("\"", "");
        // like C:... ?
        if (':' == filePath.charAt(1)) {
            filePath = filePath.substring(gitPath.length() + 1);
        }
        filePath = filePath.replaceAll("\\\\", "/");
        if ('/' == filePath.charAt(0)) {
            filePath = filePath.substring(1);
        }
        Integer line = null;
        if (filePath.contains(":")) {
            String[] split = filePath.split(":");
            filePath = split[0];
            if (split[1].length() > 0) {
                // is not int Exception
                line = Integer.parseInt(split[1]);
            }
        }
        logInfoMap.put(FILE_PATH, filePath);
        // region blame
        if (param == null
                || line != null
                || param.contains(NAME_LINE_COUNT)
                || param.contains(MORE_LINE_NAME)) {
            BlameCommand blameCommand = git.blame();
            blameCommand.setTextComparator(RawTextComparator.WS_IGNORE_ALL);
            // GitAPIException
            BlameResult blame = blameCommand.setFilePath(filePath).call();
            if (blame != null) {
                if (line != null) {
                    PersonIdent author = blame.getSourceAuthor(line - 1);
                    logInfoMap.put(BLAME_LINE, String.valueOf(line));
                    logInfoMap.put(BLAME_NAME, author.getName());
                    logInfoMap.put(BLAME_TIME, DATE_FORMAT.format(author.getWhen()));
                }
                if (param == null || param.contains(NAME_LINE_COUNT) || param.contains(MORE_LINE_NAME)) {
                    // NAME_LINE_COUNT
                    Map<String, Integer> nameLineCount = new LinkedHashMap<>();
                    for (int i = 0; i < blame.getResultContents().size(); i++) {
                        String name = blame.getSourceAuthor(i).getName();
                        mapIncrease(nameLineCount, name);
                    }
                    logInfoMap.put(NAME_LINE_COUNT, nameLineCount.toString());
                    // MORE_LINE_NAME
                    if (param == null || param.contains(MORE_LINE_NAME)) {
                        logInfoMap.put(MORE_LINE_NAME, mapMax(nameLineCount));
                    }
                }
            }
        }
        //endregion blame

        // region log
        if (param == null
                || param.contains(LAST_NAME)
                || param.contains(LAST_TIME)
                || param.contains(NAME_COMMIT_COUNT)
                || param.contains(MORE_COMMIT_NAME)) {
            LogCommand logCommand = git.log();
            // GitAPIException
            Iterator<RevCommit> revCommitIterator = logCommand.addPath(filePath).call().iterator();
            Map<String, Integer> nameCommitCount = new LinkedHashMap<>();
            while (revCommitIterator.hasNext()) {
                RevCommit revCommit = revCommitIterator.next();
                // have not NO_IGNORE_MERGE
                boolean ignoreMerge = (param == null || !param.contains(NO_IGNORE_MERGE));
                if (ignoreMerge && revCommit.getParentCount() > 1) {
                    continue;
                }
                PersonIdent author = revCommit.getAuthorIdent();
                // have not is first, just put the LAST
                String name = author.getName();
                if (!logInfoMap.containsKey(LAST_NAME)) {
                    logInfoMap.put(LAST_NAME, name);
                    logInfoMap.put(LAST_TIME, DATE_FORMAT.format(author.getWhen()));
                }
                if (param != null && !param.contains(NAME_COMMIT_COUNT)) {
                    break;
                }
                // NAME_COMMIT_COUNT
                mapIncrease(nameCommitCount, name);
                logInfoMap.put(NAME_COMMIT_COUNT, nameCommitCount.toString());
                // MORE_LINE_NAME
                if (param == null || param.contains(MORE_COMMIT_NAME)) {
                    logInfoMap.put(MORE_COMMIT_NAME, mapMax(nameCommitCount));
                }
            }
        }
        //endregion log
        return logInfoMap;
    }

    private void mapIncrease(Map<String, Integer> map, String key) {
        if (!map.containsKey(key)) {
            map.put(key, 1);
        } else {
            map.put(key, map.get(key) + 1);
        }
    }

    private String mapMax(Map<String, Integer> map) {
        String maxKey = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxKey = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return maxKey;
    }
    //endregion logInfo

    public static void main(String[] args) throws Exception {
        GitTool gitTool = new GitTool();

        String gitPath = "D:/IT/templates-for-eclipse-and-idea";
        boolean init = gitTool.init(gitPath);

        String pathAndLine = "README.md:5";

        Map<String, String> logInfo = gitTool.logInfo(pathAndLine, null);

        System.out.println(logInfo.toString().replaceAll(",", "\n"));
    }
}
