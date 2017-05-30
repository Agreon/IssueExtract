# Issue-Extract

Extracts Issues from source-code and dynamically adds and deletes them from the remote github-repository.

**Be aware, that this is a early alpha and may alter your repository-issues and your code in an unpredicted way!**

## Installation
+ `npm i -g issue-extract`
+ Go to your [Api-Tokens](https://github.com/settings/tokens) and generate a token for IssueExtract with simple repository-access (public_repo).
+ Create a `config.ie`-file in your root-direcotry with the content: `token <your_token>`

## Usage
Execute from your main git-root-directory

```
$ issue-extract <TargetDirectory> <Params>
```

**Params**
+ `-r` Close issues in repo when they are deleted in the code.

## Syntax
**Single-Line:**
```
Git-Issue: <Title> >> <Body> << [<labe1>, <label2>]`
```

**Multi-Line:**
```
Git-Issue: { 
	<Title>
	>> <Body> << 
	[<label1>, <label2>]
 }
```

Body and Labels are optional.

**If you want to help, visit the [Repo](https://github.com/Agreon/IssueExtract)**
