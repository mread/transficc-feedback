//https://github.com/paislee/healthy-gulp-angular/blob/master/gulpfile.jsvar gulp = require('gulp');

var gulp = require('gulp');
var gulpBrowser = require('gulp-browser');
var del = require('del');

var paths = {
    distFiles: 'build/resources/main/static',
    srcFiles: 'src/main/webapp/app.js'
};

gulp.task('clean', function() {
    return del(paths.distFiles);
});

gulp.task('browserify', ['clean'], function() {
    return gulp
        .src([paths.srcFiles])
        .pipe(gulpBrowser.browserify())
        .pipe(gulp.dest(paths.distFiles));
});


gulp.task('build', ['browserify']);
gulp.task('default', ['build']);