#!/usr/bin/env python3
"""Fast compile check for the MITE Twilight Forest port, safe to run from many processes at once.

Compiles with plain javac into a private temp dir (no Gradle locks), against the classpath that
`./gradlew writeCompileClasspath` wrote to build/compile-classpath.txt.

  python3 tools/javac_check.py FILE...      compile FILE... (+ any already-ported sources they reference,
                                            found through -sourcepath) and print errors for FILE...
  python3 tools/javac_check.py --all        compile every source file, print all errors
  options: --json  machine-readable output   --limit N  max errors printed per file (default 50)
           --quiet-others  hide the count of errors in files you did not ask about

Exit code 0 when the requested files have no errors.
"""
import json
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, 'src', 'main', 'java')
CP_FILE = os.path.join(ROOT, 'build', 'compile-classpath.txt')
ERR_RE = re.compile(r'^(?P<file>/[^:]+\.java):(?P<line>\d+): (?P<kind>error|warning): (?P<msg>.*)$')


def all_sources():
    out = []
    for dirpath, _, names in os.walk(SRC):
        for n in names:
            if n.endswith('.java'):
                out.append(os.path.join(dirpath, n))
    return sorted(out)


def run_javac(files):
    if not os.path.isfile(CP_FILE):
        sys.exit('missing %s - run: ./gradlew writeCompileClasspath' % CP_FILE)
    cp = open(CP_FILE).read().strip()
    with tempfile.TemporaryDirectory(prefix='tfjavac') as out_dir:
        argfile = os.path.join(out_dir, 'sources.txt')
        with open(argfile, 'w') as f:
            f.write('\n'.join('"%s"' % p for p in files))
        cmd = ['javac', '-proc:none', '-encoding', 'UTF-8', '--release', '17', '-nowarn',
               '-Xmaxerrs', '100000', '-Xmaxwarns', '0', '-implicit:none',
               '-d', os.path.join(out_dir, 'classes'), '-cp', cp, '-sourcepath', SRC, '@' + argfile]
        proc = subprocess.run(cmd, capture_output=True, text=True)
        return proc.returncode, proc.stdout + proc.stderr


def parse(output):
    errors = {}
    current = None
    for line in output.splitlines():
        m = ERR_RE.match(line)
        if m:
            if m.group('kind') != 'error':
                current = None
                continue
            current = {'line': int(m.group('line')), 'msg': m.group('msg'), 'detail': []}
            errors.setdefault(os.path.realpath(m.group('file')), []).append(current)
        elif current is not None and line and not re.match(r'^\d+ errors?$', line):
            if len(current['detail']) < 6:
                current['detail'].append(line.rstrip())
    return errors


def main(argv):
    as_json = '--json' in argv
    quiet_others = '--quiet-others' in argv
    limit = 50
    if '--limit' in argv:
        limit = int(argv[argv.index('--limit') + 1])
    args = [a for i, a in enumerate(argv) if not a.startswith('--') and not (i > 0 and argv[i - 1] == '--limit')]
    if '--all' in argv or not args:
        files = all_sources()
        wanted = set(os.path.realpath(f) for f in files)
    else:
        files = [os.path.abspath(a) for a in args]
        missing = [f for f in files if not os.path.isfile(f)]
        if missing:
            sys.exit('no such file: ' + ', '.join(missing))
        wanted = set(os.path.realpath(f) for f in files)
    code, output = run_javac(files)
    errors = parse(output)
    mine = {f: e for f, e in errors.items() if f in wanted}
    others = {f: e for f, e in errors.items() if f not in wanted}
    if as_json:
        print(json.dumps({'errors': {os.path.relpath(f, ROOT): e for f, e in sorted(mine.items())},
                          'error_count': sum(len(e) for e in mine.values()),
                          'other_files_with_errors': {os.path.relpath(f, ROOT): len(e) for f, e in sorted(others.items())},
                          'javac_exit': code}, indent=1))
    else:
        for f, errs in sorted(mine.items()):
            print('== %s (%d errors)' % (os.path.relpath(f, ROOT), len(errs)))
            for e in errs[:limit]:
                print('  %d: %s' % (e['line'], e['msg']))
                for d in e['detail']:
                    print('      ' + d)
            if len(errs) > limit:
                print('  ... %d more' % (len(errs) - limit))
        total = sum(len(e) for e in mine.values())
        print('TOTAL errors in requested files: %d' % total)
        if others and not quiet_others:
            print('(also %d errors in %d other files pulled in via -sourcepath, not shown)'
                  % (sum(len(e) for e in others.values()), len(others)))
        if code != 0 and not errors:
            print(output[-4000:])
    sys.exit(0 if not mine and (code == 0 or others) else 1)


if __name__ == '__main__':
    main(sys.argv[1:])
