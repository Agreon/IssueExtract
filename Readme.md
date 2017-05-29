# Issue-Extract

Extracts Issues from source-code and dynamically adds and deletes them from the remote github-repository.

**Be aware, that this is a early alpha and may alter your repository-issues in an unpredicted way.**
**It closes Issues, that are not found in the code, if not specified through a paramter!**

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
+ `-r=bool` remove issues from repo when deleted in code. Standart = true 

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
