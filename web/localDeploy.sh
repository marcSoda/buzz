# local deploy script for the web front-end

# This file is responsible for preprocessing all TypeScript files, making
# sure all dependencies are up-to-date, copying all necessary files into a
# local deploy directory, and starting a web server

# This is the resource folder where maven expects to find our files
TARGETFOLDER=./local

# step 1: update our npm dependencies
# npm update

# step 2: make sure we have someplace to put everything.  We will delete the
#         old folder, and then make it from scratch
rm -rf $TARGETFOLDER
mkdir $TARGETFOLDER

# step 3: copy static html, css, and JavaScript files
cp index.html login.html $TARGETFOLDER
cp node_modules/jquery/dist/jquery.min.js $TARGETFOLDER
cp node_modules/jquery/dist/jquery.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/handlebars/dist/handlebars.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/bootstrap/dist/js/bootstrap.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/bootstrap/dist/css/bootstrap.min.css $TARGETFOLDER/$WEBFOLDERNAME
cp -R node_modules/bootstrap/dist/fonts $TARGETFOLDER/$WEBFOLDERNAME
cat app.css login.css css/UserPage.css css/ElementList.css css/EditEntryForm.css css/NewEntryForm.css css/Navbar.css css/EntryMenu.css css/CommentList.css > $TARGETFOLDER/$WEBFOLDERNAME/app.css
cp favicon.ico $TARGETFOLDER/$WEBFOLDERNAME

# step 4: compile handlebars templates to the deploy folder
node_modules/handlebars/bin/handlebars hb/ElementList.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js
node_modules/handlebars/bin/handlebars hb/EditEntryForm.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js
node_modules/handlebars/bin/handlebars hb/NewEntryForm.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js
node_modules/handlebars/bin/handlebars hb/UserPage.hb hb/Navbar.hb hb/EntryMenu.hb hb/CommentList.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js

# step 5: compile TypeScript files
node_modules/.bin/tsc app.ts --strict --outFile $TARGETFOLDER/app.js
node_modules/.bin/tsc login.ts --strict --outFile $TARGETFOLDER/login.js

# # step 6: compile tests and copy tests to the local deploy folder
# node_modules/.bin/tsc apptest.ts --strict --outFile $TARGETFOLDER/apptest.js
# cp spec_runner.html $TARGETFOLDER
# cp node_modules/jasmine-core/lib/jasmine-core/jasmine.css $TARGETFOLDER
# cp node_modules/jasmine-core/lib/jasmine-core/jasmine.js $TARGETFOLDER
# cp node_modules/jasmine-core/lib/jasmine-core/boot.js $TARGETFOLDER
# cp node_modules/jasmine-core/lib/jasmine-core/jasmine-html.js $TARGETFOLDER

# step 7: launch the server.  Be sure to disable caching
# (Note: we don't currently use -s for silent operation)
node_modules/.bin/http-server $TARGETFOLDER -c-1
