//https://github.com/paislee/healthy-gulp-angular/blob/master/gulpfile.jsvar gulp = require('gulp');

var gulp = require('gulp');
var gulpBrowser = require('gulp-browser');
var del = require('del');
var qunit = require('gulp-qunit');

var paths = {
    distFiles: 'build/resources/main/static',
    srcFiles: 'src/main/webapp/app.js',
    testFiles: 'src/test/webapp/test-runner.html'
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

gulp.task('test', function() {
    return gulp
        .src([paths.testFiles])
        .pipe(qunit());
});

gulp.task('build', ['browserify', 'test']);
gulp.task('default', ['build']);
