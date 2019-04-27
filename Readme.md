A LR1 Compiler by Dootmaan
===

- WordAnanlyzer should be used with SemanticAnanlyzer or GrammarAnalyzer(but not both of them) to complete the whole function. 

- If used with GrammarAnalyzer then the compiler won't do any semantic analysis. But if used with  SemanticAnanlyzer it will also do the grammar analysis.

- Main method is in package word. After running this method you will be asked to choose a file to analysis. The results will be displayed in the console.

- This Program is OK to custonmize the Grammar. Changed the LR1 or SLR grammar by changing the parameter in the constructor of GrammarAnalyzer or SemanticAnanlyzer.

- In a word, SemanticAnanlyzer is based on GrammarAnalyzer so it inherits all the functions of GrammarAnalyzer.
