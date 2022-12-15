 
listView('project-1 Jobs') {
    description('project-1 Jobs')
    jobs {
        regex('project-1_.+')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
