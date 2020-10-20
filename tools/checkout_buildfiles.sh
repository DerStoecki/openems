# Prepares a Commit
#
# - Adds .gitignore file to empty test directories.
# 
#   When Eclipse creates 'test' src folders they are sometimes empty. Empty 
#   folders are not committed to GIT. Because of this Eclipse would show errors
#   when importing the projects. This script creates an empty '.gitignore' file
#   inside each 'test' folder to solve this.
#
#   See https://stackoverflow.com/questions/115983
# 
# - Resets .classpath files.
#
#   When Eclipse 'Build All' is called, all .classpath files are touched and
#   unnecessarily marked as changed. Using this script those files are reset
#   to origin.
#
for D in *; do
	if [ -d "${D}" ]; then
		case "${D}" in
			build|cnf|doc|edge|ui|tools)
				;;
			*)
				if [ -f  "${D}/generated/buildfiles" ]; then
				git checkout  ${D}/generated/buildfiles
				fi
				;;
		esac
	fi
done
