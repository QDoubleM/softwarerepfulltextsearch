package indexcreater;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class OpenRepository {
	
	@SuppressWarnings("unused")
	public static Repository openRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Git git = Git.open(new File("E:/git仓库/.git"));
        return git.getRepository();
	}
	public static void listAllBanches() throws GitAPIException, IOException{
		try (Repository repository = openRepository()) {
            System.out.println("Listing local branches:");
            try (Git git = new Git(repository)) {
                List<Ref> call = git.branchList().call();
                for (Ref ref : call) {
                    System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
                }
                System.out.println("Now including remote branches:");
                call = git.branchList().setListMode(ListMode.ALL).call();
                for (Ref ref : call) {
                    System.out.println("Branch: " + ref + ";" + ref.getName() + ":" + ref.getObjectId().getName());
                }
            }
        }
	}
	
	public static void getUserConfigInfo() throws IOException{
		try (Repository repository = openRepository()) {
            Config config = repository.getConfig();
            String name = config.getString("user", null, "name");
            String email = config.getString("user", null, "email");
            if (name != null) {
                System.out.println("User identity is " + name + " <" + email + ">");
            }
            String url = config.getString("remote", "origin", "url");
            if (url != null) {
            	System.out.println("Origin comes from " + url);
            }
        }
	}
	
	public static void readBlobContents() throws IOException{
		try (Repository repository = openRepository()) {
            // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
            Ref head = repository.exactRef("refs/heads/master");
            System.out.println("Ref of refs/heads/master: " + head);

            System.out.println("\nPrint contents of head of master branch, i.e. the latest commit information");
            ObjectLoader loader = repository.open(head.getObjectId());
            loader.copyTo(System.out);

            System.out.println("\nPrint contents of tree of head of master branch, i.e. the latest binary tree information");

            // a commit points to a tree
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(head.getObjectId());
                RevTree tree = walk.parseTree(commit.getTree().getId());
                System.out.println("Found Tree: " + tree);
                loader = repository.open(tree.getId());
                byte[] bytes = loader.getBytes();
                System.out.println(new String(bytes,"GB2312"));
                walk.dispose();
            }
        }
	}
	
	public static void main(String[] args) throws IOException, GitAPIException {

		String filename = "2.txt";
		try (Repository repository = openRepository()) {
			// find the HEAD
			ObjectId lastCommitId = repository.resolve(Constants.HEAD);
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit commit = revWalk.parseCommit(lastCommitId);
				RevTree tree = commit.getTree();
				System.out.println("Having tree: " + tree);
				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filename));
					if (!treeWalk.next()) {
						throw new IllegalStateException(
								"Did not find expected file 'README.md'");
					}
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] bytes = loader.getBytes();
					System.out.println(new String(bytes, "GB2312"));
				}
				revWalk.dispose();
			}
		}
        //listAllBanches();
        //getUserConfigInfo();
        readBlobContents();
    }
}
