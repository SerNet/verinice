.SUFFIXES: .pdf .tex .zip

TEX=pdflatex
BIN=bin
TEXFLAGS=-output-directory $(BIN) -interaction=batchmode

.tex.pdf:
	mkdir -p $(BIN)
	$(TEX) $(TEXFLAGS) $<
	$(TEX) $(TEXFLAGS) $<
	cp $(BIN)/$@ .

.tex.zip:
	rm -rf $(BIN)/$* # latex2html seems to need a clean build dir
	mkdir -p $(BIN)/$*
	latex2html -dir $(BIN)/$* -init_file=latex2html.init $< >/dev/null
	# copy icons without text
	cp Icon/contents.png $(BIN)/$*
	cp Icon/next.png $(BIN)/$*
	cp Icon/up.png $(BIN)/$*
	cp Icon/up.png $(BIN)/$*/up_g.png
	cp Icon/prev.png $(BIN)/$*
	cp Icon/prev.png $(BIN)/$*/prev_g.png
	zip -q -r $(BIN)/$@ $(BIN)/$*
	cp $(BIN)/$@ .

