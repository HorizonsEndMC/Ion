[Go Back](../README.md)

# Making changes

As a means of quality control, the `master` branch cannot be committed to by
all developers. Instead, there is a process you must follow.

To better understand this part, please see the [GitHub flow](https://guides.github.com/introduction/flow/)
guide. We loosely follow GitHub flow, although not entirely. Mainly, you should go through that
guide to learn how branches and pull requests work. For general instructions
on how to use Git through IntelliJ IDEA's GUI, see [the official help documentation](https://www.jetbrains.com/help/idea/using-git-integration.html).

## Creating a Branch

Since we're using IntelliJ IDEA, you don't necessarily need to use the Git
commands to create a branch, although we highly recommend learning how to use
the Git command-line interface. For now, here is how to create a branch in
IntelliJ IDEA. For more details about managing branches in IDEA, see the
[official guide](https://www.jetbrains.com/help/idea/manage-branches.html).

1. Click "VCS" on the top menu bar, hover to "Git", and click on "Branches".

![Image of VCS Dropdown](https://i.imgur.com/kurLwcM.png)

2. Click "New Branch".

![Image of New Branch Button](https://i.imgur.com/E4yCUHy.png)

3. Enter the name of your new branch and click create. The name should match
what the branch does; the branch should only make a certain set of related
changes. If you need to make other changes, checkout the `master` branch and
make a new branch. (Leave "checkout branch" checked; it'll automatically
checkout [or "open"] the branch for you.)

![Image of New Branch Menu](https://i.imgur.com/2Bj2wY5.png)

Now you can make changes to your branch without affecting the `master` branch.

## Making Commits

After making changes, you need to commit them to the branch. If you don't know
how commits work, read [this GitHub guide about commits](https://github.com/git-guides/git-commit).
To see how to create commits using IntelliJ IDEA's GUI, please see
[this guide](https://www.jetbrains.com/help/idea/commit-and-push-changes.html#commit).

## Pushing Changes

While you can keep the branch to just your computer, you should upload your
changes whenever you're finished so that you don't lose them. This also
allows you to open a pull request to merge your changes to the `master` branch.

> Note: Before pushing, you will need to make some commits.

1. Click "VCS" on the top menu bar, hover to "Git", and click on "Push".

![Image of Push Menu](https://i.imgur.com/YaItr3l.png)

2. (Optional) Look over your changes by clicking on the modified files and
double checking what you changed.

3. Click "Push".

## Updating Your Branch

If someone were to make changes to the `master` branch, these changes wouldn't
be reflected automatically in your own branch. Instead, you will have to
*merge* (or, alternatively, rebase; but don't worry about that for now) the
latest `master` branch into your branch. This applies even for your own branch:
If someone updates the repository's version of your branch, you will need to
merge changes into your own branch.

The simplest way to do this is to pull changes (and then merge the master branch
into your own, if applicable). If you don't know how pulling works, please see
[this guide](https://github.com/git-guides/git-pull).

1. Click the "Update Project" button on the top right of IDEA.

![Image of Update Project Button](https://i.imgur.com/ueXmpun.png)

2. Click "OK" (you can probably leave the setting(s) untouched).

3. (For non-master branches only) Open the "Branches" menu as before, click on
"origin/master" (not just "master", because that's just your own local copy of
the `master` branch), and click "Merge into Current".

![Image of Merge Button](https://i.imgur.com/2Ly7rTp.png).

4. If files you changed were also changed on the `master` branch you may run in
to *merge conflicts*. To learn more about merge conflicts, see
[here](https://docs.github.com/en/free-pro-team@latest/github/collaborating-with-issues-and-pull-requests/about-merge-conflicts).
To learn how to use IntelliJ IDEA's system for resolving Git merge conflicts,
see [this guide on JetBrain's website](https://www.jetbrains.com/help/idea/resolve-conflicts.html).

[Go Back](../README.md)
